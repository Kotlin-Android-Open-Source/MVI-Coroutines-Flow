package com.hoc.flowmvi.ui.search

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.takeUntil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
class SearchVM(
  private val searchUsersUseCase: SearchUsersUseCase,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = ViewState.initial()

    viewState = intentFlow
      .toPartialStateChangesFlow()
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .catch { Log.d(logTag, "[SEARCH_VM] Throwable: $it") }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)
  }

  private fun Flow<ViewIntent>.toPartialStateChangesFlow(): Flow<PartialStateChange> {
    val executeSearch: suspend (String) -> Flow<PartialStateChange> = { query: String ->
      flow { emit(searchUsersUseCase(query)) }
        .map { result ->
          result.fold(
            ifLeft = { PartialStateChange.Failure(it, query) },
            ifRight = {
              PartialStateChange.Success(
                it.map(UserItem::from),
                query
              )
            }
          )
        }
        .onStart { emit(PartialStateChange.Loading) }
    }

    val queryFlow = filterIsInstance<ViewIntent.Search>()
      .debounce(Duration.milliseconds(400))
      .map { it.query }
      .filter { it.isNotBlank() }
      .distinctUntilChanged()
      .shareWhileSubscribed()

    return merge(
      queryFlow.flatMapLatest(executeSearch),
      filterIsInstance<ViewIntent.Retry>()
        .flatMapFirst {
          viewState.value.let { vs ->
            if (vs.error !== null) executeSearch(vs.query).takeUntil(queryFlow)
            else emptyFlow()
          }
        },
    )
  }

  private fun Flow<PartialStateChange>.sendSingleEvent(): Flow<PartialStateChange> =
    onEach { change ->
      when (change) {
        is PartialStateChange.Failure -> sendEvent(SingleEvent.SearchFailure(change.error))
        PartialStateChange.Loading -> return@onEach
        is PartialStateChange.Success -> return@onEach
      }
    }
}
