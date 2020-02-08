package com.hoc.flowmvi.ui

import android.util.Log
import androidx.lifecycle.*
import com.hoc.flowmvi.Event
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.flatMapFirst
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class MainVM(
    private val getUsersUseCase: GetUsersUseCase,
    private val refreshGetUsersUseCase: RefreshGetUsersUseCase,
    private val removeUserUseCase: RemoveUserUseCase
) : ViewModel() {
  private val initialVS = ViewState.initial()

  private val _viewStateD = MutableLiveData<ViewState>()
      .apply { value = initialVS }
  val viewState: LiveData<ViewState> = _viewStateD.distinctUntilChanged()

  private val _eventD = MutableLiveData<Event<SingleEvent>>()
  val singleEvent: LiveData<Event<SingleEvent>> get() = _eventD

  private val _intentChannel = BroadcastChannel<ViewIntent>(capacity = Channel.CONFLATED)
  suspend fun processIntent(intent: ViewIntent) = _intentChannel.send(intent)

  init {
    val intentFlow = _intentChannel.asFlow()

    merge(
        intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
        intentFlow.filterNot { it is ViewIntent.Initial }
    )
        .toPartialChangeFlow()
        .sendSingleEvent()
        .scan(initialVS) { vs, change -> change.reduce(vs) }
        .onEach { _viewStateD.value = it }
        .launchIn(viewModelScope)
  }

  private fun Flow<PartialChange>.sendSingleEvent(): Flow<PartialChange> {
    return onEach {
      _eventD.value = when (it) {
        is PartialChange.GetUser.Error -> Event(SingleEvent.GetUsersError(it.error))
        is PartialChange.Refresh.Success -> Event(SingleEvent.Refresh.Success)
        is PartialChange.Refresh.Failure -> Event(SingleEvent.Refresh.Failure(it.error))
        is PartialChange.RemoveUser.Success -> Event(SingleEvent.RemoveUser.Success(it.user))
        is PartialChange.RemoveUser.Failure -> Event(
            SingleEvent.RemoveUser.Failure(
                user = it.user,
                error = it.error
            )
        )
        PartialChange.GetUser.Loading -> return@onEach
        is PartialChange.GetUser.Data -> return@onEach
        PartialChange.Refresh.Loading -> return@onEach
      }
    }
  }

  private fun <T> Flow<T>.toPartialChangeFlow(): Flow<PartialChange> {
    val getUserChanges = getUsersUseCase()
        .onEach { Log.d("###", "[MAIN_VM] Emit users.size=${it.size}") }
        .map {
          val items = it.map(::UserItem)
          PartialChange.GetUser.Data(items) as PartialChange.GetUser
        }
        .onStart { emit(PartialChange.GetUser.Loading) }
        .catch { emit(PartialChange.GetUser.Error(it)) }

    val refreshChanges = flow { emit(refreshGetUsersUseCase()) }
        .map { PartialChange.Refresh.Success as PartialChange.Refresh }
        .onStart { emit(PartialChange.Refresh.Loading) }
        .catch { emit(PartialChange.Refresh.Failure(it)) }

    return merge(
        filterIsInstance<ViewIntent.Initial>()
            .logIntent()
            .flatMapConcat { getUserChanges },
        filterIsInstance<ViewIntent.Refresh>()
            .filter { _viewStateD.value?.let { !it.isLoading && it.error === null } ?: false }
            .logIntent()
            .flatMapFirst { refreshChanges },
        filterIsInstance<ViewIntent.Retry>()
            .filter { _viewStateD.value?.error != null }
            .logIntent()
            .flatMapFirst { getUserChanges },
        filterIsInstance<ViewIntent.RemoveUser>()
            .logIntent()
            .map { it.user }
            .flatMapMerge { userItem ->
              flow {
                try {
                  removeUserUseCase(userItem.toDomain())
                  emit(PartialChange.RemoveUser.Success(userItem))
                } catch (e: Exception) {
                  emit(PartialChange.RemoveUser.Failure(userItem, e))
                }
              }
            }
    )
  }

  private fun <T : ViewIntent> Flow<T>.logIntent() = onEach { Log.d("MainVM", "## Intent: $it") }
}


