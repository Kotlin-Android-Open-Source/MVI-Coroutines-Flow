package com.hoc.flowmvi.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.hoc081098.flowext.takeUntil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
class SearchVM(
  private val searchUsersUseCase: SearchUsersUseCase,
  savedStateHandle: SavedStateHandle,
  private val stateSaver: ViewState.StateSaver,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val rawLogTag get() = "SearchVM[${System.identityHashCode(this)}]"

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = stateSaver.restore(savedStateHandle[VIEW_STATE_BUNDLE_KEY])

    viewState = intentSharedFlow
      .debugLog("ViewIntent")
      .toPartialStateChangeFlow()
      .debugLog("PartialStateChange")
      .onEach { sendEvent(it.toSingleEventOrNull() ?: return@onEach) }
      .scan(initialVS) { state, change -> change.reduce(state) }
      .debugLog("ViewState")
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)

    savedStateHandle.setSavedStateProvider(VIEW_STATE_BUNDLE_KEY) {
      stateSaver.run { viewState.value.toBundle() }
    }
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(): Flow<PartialStateChange> {
    val queryFlow = filterIsInstance<ViewIntent.Search>()
      .map { it.query }
      .shareWhileSubscribed()

    val searchableQueryFlow = queryFlow
      .debounce(SEARCH_DEBOUNCE_DURATION)
      .filter { it.isNotBlank() }
      .distinctUntilChanged()
      .shareWhileSubscribed()

    return merge(
      // Search change
      searchableQueryFlow
        .flatMapLatest(::executeSearch),
      // Retry change
      filterIsInstance<ViewIntent.Retry>()
        .toPartialStateChangeFlow(searchableQueryFlow),
      // Query change
      queryFlow
        .map { PartialStateChange.QueryChange(it) },
    )
  }

  //region Processors
  private fun Flow<ViewIntent.Retry>.toPartialStateChangeFlow(searchableQueryFlow: SharedFlow<String>): Flow<PartialStateChange> =
    flatMapFirst {
      viewState.value.let { vs ->
        if (vs.error !== null) {
          executeSearch(vs.submittedQuery).takeUntil(searchableQueryFlow)
        } else {
          emptyFlow()
        }
      }
    }
  //endregion

  private fun executeSearch(query: String) = flowFromSuspend { searchUsersUseCase(query) }
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
    .startWith(PartialStateChange.Loading)

  internal companion object {
    private const val VIEW_STATE_BUNDLE_KEY = "com.hoc.flowmvi.ui.search.view_state"
    internal val SEARCH_DEBOUNCE_DURATION = 400.milliseconds

    private fun PartialStateChange.toSingleEventOrNull(): SingleEvent? = when (this) {
      is PartialStateChange.Failure -> SingleEvent.SearchFailure(error)
      PartialStateChange.Loading,
      is PartialStateChange.Success,
      is PartialStateChange.QueryChange,
      -> null
    }
  }
}
