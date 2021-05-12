package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.domain.entity.User

internal sealed interface ViewIntent {
  data class Search(val query: String) : ViewIntent
  object Retry : ViewIntent
}

internal data class ViewState(
  val users: List<User>,
  val isLoading: Boolean,
  val error: Throwable?
) {
  companion object Factory {
    fun initial(): ViewState {
      return ViewState(
        users = emptyList(),
        isLoading = false,
        error = null,
      )
    }
  }
}

internal sealed interface PartialStateChange {
  fun reduce(state: ViewState): ViewState

  sealed class Search : PartialStateChange {
    object Loading : Search()
    data class Success(val users: List<User>) : Search()
    data class Failure(val error: Throwable) : Search()

    override fun reduce(state: ViewState) = when (this) {
      is Failure -> state.copy(isLoading = false, error = error)
      Loading -> state.copy(isLoading = true, error = null)
      is Success -> state.copy(isLoading = false, error = null, users = users)
    }
  }
}
