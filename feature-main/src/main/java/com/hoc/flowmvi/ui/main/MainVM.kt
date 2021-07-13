package com.hoc.flowmvi.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc081098.flowext.flatMapFirst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take

@Suppress("USELESS_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
internal class MainVM(
  private val getUsersUseCase: GetUsersUseCase,
  private val refreshGetUsers: RefreshGetUsersUseCase,
  private val removeUser: RemoveUserUseCase,
) : ViewModel() {
  private val _eventChannel = Channel<SingleEvent>(Channel.BUFFERED)
  private val _intentFlow = MutableSharedFlow<ViewIntent>(extraBufferCapacity = 64)

  val viewState: StateFlow<ViewState>
  val singleEvent: Flow<SingleEvent> get() = _eventChannel.receiveAsFlow()

  fun processIntent(intent: ViewIntent) = _intentFlow.tryEmit(intent)

  init {
    val initialVS = ViewState.initial()

    viewState = merge(
      _intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
      _intentFlow.filterNot { it is ViewIntent.Initial }
    )
      .toPartialChangeFlow()
      .sendSingleEvent()
      .scan(initialVS) { vs, change -> change.reduce(vs) }
      .catch { Log.d("###", "[MAIN_VM] Throwable: $it") }
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialVS
      )
  }

  private fun Flow<PartialChange>.sendSingleEvent(): Flow<PartialChange> {
    return onEach {
      val event = when (it) {
        is PartialChange.GetUser.Error -> SingleEvent.GetUsersError(it.error)
        is PartialChange.Refresh.Success -> SingleEvent.Refresh.Success
        is PartialChange.Refresh.Failure -> SingleEvent.Refresh.Failure(it.error)
        is PartialChange.RemoveUser.Success -> SingleEvent.RemoveUser.Success(it.user)
        is PartialChange.RemoveUser.Failure -> SingleEvent.RemoveUser.Failure(
          user = it.user,
          error = it.error,
        )
        PartialChange.GetUser.Loading -> return@onEach
        is PartialChange.GetUser.Data -> return@onEach
        PartialChange.Refresh.Loading -> return@onEach
      }
      _eventChannel.send(event)
    }
  }

  private fun Flow<ViewIntent>.toPartialChangeFlow(): Flow<PartialChange> = shareIn(viewModelScope, SharingStarted.WhileSubscribed()).run {
    val getUserChanges = getUsersUseCase()
      .onEach { Log.d("###", "[MAIN_VM] Emit users.size=${it.size}") }
      .map {
        val items = it.map(::UserItem)
        PartialChange.GetUser.Data(items) as PartialChange.GetUser
      }
      .onStart { emit(PartialChange.GetUser.Loading) }
      .catch { emit(PartialChange.GetUser.Error(it)) }

    val refreshChanges = refreshGetUsers::invoke
      .asFlow()
      .map { PartialChange.Refresh.Success as PartialChange.Refresh }
      .onStart { emit(PartialChange.Refresh.Loading) }
      .catch { emit(PartialChange.Refresh.Failure(it)) }

    return merge(
      filterIsInstance<ViewIntent.Initial>()
        .logIntent()
        .flatMapConcat { getUserChanges },
      filterIsInstance<ViewIntent.Refresh>()
        .filter { viewState.value.let { !it.isLoading && it.error === null } }
        .logIntent()
        .flatMapFirst { refreshChanges },
      filterIsInstance<ViewIntent.Retry>()
        .filter { viewState.value.error != null }
        .logIntent()
        .flatMapFirst { getUserChanges },
      filterIsInstance<ViewIntent.RemoveUser>()
        .logIntent()
        .map { it.user }
        .flatMapMerge { userItem ->
          flow {
            userItem
              .toDomain()
              .let { removeUser(it) }
              .let { emit(it) }
          }
            .map { PartialChange.RemoveUser.Success(userItem) as PartialChange.RemoveUser }
            .catch { emit(PartialChange.RemoveUser.Failure(userItem, it)) }
        }
    )
  }

  private fun <T : ViewIntent> Flow<T>.logIntent() = onEach { Log.d("MainVM", "## Intent: $it") }
}
