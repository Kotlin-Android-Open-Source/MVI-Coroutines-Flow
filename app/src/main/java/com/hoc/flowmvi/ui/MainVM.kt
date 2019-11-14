package com.hoc.flowmvi.ui

import android.util.Log
import androidx.lifecycle.*
import com.hoc.flowmvi.Event
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.flatMapFirst
import com.hoc.flowmvi.merge
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

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

    val refreshChanges = flow { emit(getUsersUseCase()) }
      .map {
        val items = it.map(::UserItem)
        PartialChange.Refresh.Success(items) as PartialChange.Refresh
      }
      .onStart { emit(PartialChange.Refresh.Loading) }
      .catch { emit(PartialChange.Refresh.Failure(it)) }

    return merge(
      filterIsInstance<ViewIntent.Initial>().logIntent().flatMapConcat { getUserChanges },
      filterIsInstance<ViewIntent.Refresh>().logIntent().flatMapFirst { refreshChanges },
      filterIsInstance<ViewIntent.Retry>()
        .filter { viewState.value.let { it !== null && it.error === null } }
        .logIntent()
        .flatMapFirst { getUserChanges }
    )
  }

  private fun Flow<ViewIntent>.logIntent() = onEach { Log.d("MainVM", "## Intent: $it") }
}


