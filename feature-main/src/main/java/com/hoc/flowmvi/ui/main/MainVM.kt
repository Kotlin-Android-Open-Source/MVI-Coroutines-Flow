package com.hoc.flowmvi.ui.main

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.core.flatMapFirst
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.compose
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import java.util.function.BiFunction

@Suppress("USELESS_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
internal class MainVM(
  private val getUsersUseCase: GetUsersUseCase,
  private val refreshGetUsers: RefreshGetUsersUseCase,
  private val removeUser: RemoveUserUseCase,
) : BaseVM<ViewState>() {
  private val _intentFlow = MutableSharedFlow<ViewIntent>(extraBufferCapacity = 64)

  override val viewState: StateFlow<ViewState>

  val userFlow = flowOf(1, 3, 6)

  suspend fun processIntent(intent: ViewIntent) = _intentFlow.emit(intent)

  init {
    val initialVS = ViewState.initial()
    viewState = merge(
      _intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
      _intentFlow.filterNot { it is ViewIntent.Initial }
    )
      .toPartialChangeFlow()
      .reduce(initialVS)
      .catch { Log.d("###", "[MAIN_VM] Throwable: $it") }
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialVS
      )
  }

  private fun Flow<PartialChange>.reduce(viewState: ViewState): Flow<ViewState> {
    return this.scan(viewState) { vs, change ->
      val resetViewState = vs.copy(shouldOpenNextScreen = false)
      when (change) {
        is PartialChange.GetUser.Data -> resetViewState.copy(
          isLoading = false,
          error = null,
          userItems = change.users,
          shouldOpenNextScreen = true
        )
        is PartialChange.GetUser.Loading -> resetViewState.copy(isLoading = true, error = null)
        is PartialChange.GetUser.Error -> resetViewState.copy(
          isLoading = false,
          error = change.error
        )
        is PartialChange.Refresh.Success -> resetViewState.copy(isRefreshing = false)
        is PartialChange.Refresh.Failure -> resetViewState.copy(isRefreshing = false)
        is PartialChange.Refresh.Loading -> resetViewState.copy(isRefreshing = true)
        else -> resetViewState
      }
    }
  }

  private fun Flow<ViewIntent>.toPartialChangeFlow(): Flow<PartialChange> =
    shareIn(viewModelScope, SharingStarted.WhileSubscribed()).run {
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
        filterIsInstance<ViewIntent.Initial>().initialProcessor(),
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

  private fun Flow<ViewIntent.Initial>.initialProcessor(): Flow<PartialChange> =
    flatMapMerge {
      getUsersUseCase.invoke().map {
        PartialChange.GetUser.Data(it.map(::UserItem))
      }
    }

  private fun <T : ViewIntent> Flow<T>.logIntent() = onEach { Log.d("MainVM", "## Intent: $it") }
}
