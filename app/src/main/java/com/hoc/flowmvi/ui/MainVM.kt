package com.hoc.flowmvi.ui

import android.util.Log
import androidx.lifecycle.*
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
  val viewState get() = _viewStateD.distinctUntilChanged()

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
        .scan(initialVS) { vs, change -> change.reduce(vs) }
        .collect { _viewStateD.value = it }
    }
  }


  private fun <T> Flow<T>.toPartialChangeFlow(): Flow<PartialChange> {
    return filterIsInstance<ViewIntent.Initial>().flatMapConcat {
      flow {
        emit(PartialChange.GetUser.Loading)
        try {
          emit(getUsersUseCase().map(::UserItem).let { PartialChange.GetUser.Data(it) })
        } catch (e: Throwable) {
          emit(PartialChange.GetUser.Error(e))
        }
      }
    }
  }
}

