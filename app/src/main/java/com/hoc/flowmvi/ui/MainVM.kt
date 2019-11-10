package com.hoc.flowmvi.ui

import android.util.Log
import androidx.lifecycle.*
import com.hoc.flowmvi.Event
import com.hoc.flowmvi.domain.GetUsersUseCase
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class MainVM(private val getUsersUseCase: GetUsersUseCase) : ViewModel() {
  private val initialVS = ViewState.initial()

  private val _viewStateD = MutableLiveData<ViewState>()
    .apply { value = initialVS }
  val viewState: LiveData<ViewState> get() = _viewStateD.distinctUntilChanged()

  private val _eventD = MutableLiveData<Event<SingleEvent>>()
  val singleEvent: LiveData<Event<SingleEvent>> get() = _eventD

  private val _intentChannel = BroadcastChannel<ViewIntent>(capacity = Channel.CONFLATED)
  suspend fun processIntent(intent: ViewIntent) = _intentChannel.send(intent)

  init {
    val intentFlow = _intentChannel.asFlow()

    val flows = listOf(
      intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
      intentFlow.filterNot { it is ViewIntent.Initial }
    )

    viewModelScope.launch {
      flows
        .asFlow()
        .flattenMerge(flows.size)
        .onEach { Log.d("MainVM", "Intent $it") }
        .toPartialChangeFlow()
        .sendSingleEvent()
        .scan(initialVS) { vs, change -> change.reduce(vs) }
        .collect { _viewStateD.value = it }
    }
  }

  private fun <T> Flow<T>.sendSingleEvent(): Flow<T> {
    return onEach {
      when (it) {
        is PartialChange.GetUser.Error -> {
          _eventD.value = Event(SingleEvent.GetUsersError(it.error))
        }
        is PartialChange.Refresh.Success -> {
          _eventD.value = Event(SingleEvent.Refresh.Success)
        }
        is PartialChange.Refresh.Failure -> {
          _eventD.value = Event(SingleEvent.Refresh.Failure(it.error))
        }
      }
    }
  }

  private fun <T> Flow<T>.toPartialChangeFlow(): Flow<PartialChange> {
    val getUserChanges = flow {
      emit(PartialChange.GetUser.Loading)
      try {
        emit(getUsersUseCase().map(::UserItem).let { PartialChange.GetUser.Data(it) })
      } catch (e: Throwable) {
        emit(PartialChange.GetUser.Error(e))
      }
    }

    val refreshChanges = flow {
      emit(PartialChange.Refresh.Loading)
      try {
        emit(getUsersUseCase().map(::UserItem).let { PartialChange.Refresh.Success(it) })
      } catch (e: Throwable) {
        emit(PartialChange.Refresh.Failure(e))
      }
    }

    val flows = listOf(
      filterIsInstance<ViewIntent.Initial>().flatMapConcat { getUserChanges },
      filterIsInstance<ViewIntent.Refresh>().flatMapConcat { refreshChanges }
    )

    return flows.asFlow().flattenMerge(flows.size)
  }
}


