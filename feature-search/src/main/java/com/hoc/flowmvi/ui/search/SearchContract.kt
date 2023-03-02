package com.hoc.flowmvi.ui.search

import android.os.Bundle
import androidx.core.os.bundleOf
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import com.hoc.flowmvi.mvi_base.MviViewStateSaver
import dev.drewhamilton.poko.Poko

@Poko class UserItem private constructor(
  val id: String,
  val email: String,
  val avatar: String,
  val fullName: String,
) {
  companion object Factory {
    fun from(domain: User): UserItem {
      return UserItem(
        id = domain.id,
        email = domain.email.value,
        avatar = domain.avatar,
        fullName = "${domain.firstName.value} ${domain.lastName.value}",
      )
    }
  }
}

sealed interface ViewIntent : MviIntent {
  data class Search(val query: String) : ViewIntent
  object Retry : ViewIntent
}

data class ViewState(
  val users: List<UserItem>,
  val isLoading: Boolean,
  val error: UserError?,
  val submittedQuery: String,
  val originalQuery: String,
) : MviViewState {
  companion object Factory {
    private const val ORIGINAL_QUERY_KEY = "com.hoc.flowmvi.ui.search.original_query"

    fun initial(originalQuery: String): ViewState {
      return ViewState(
        users = emptyList(),
        isLoading = false,
        error = null,
        submittedQuery = "",
        originalQuery = originalQuery,
      )
    }
  }

  class StateSaver : MviViewStateSaver<ViewState> {
    override fun ViewState.toBundle() = bundleOf(ORIGINAL_QUERY_KEY to originalQuery)

    override fun restore(bundle: Bundle?) = initial(
      originalQuery = bundle
        ?.getString(ORIGINAL_QUERY_KEY, "")
        .orEmpty(),
    )
  }
}

internal sealed interface PartialStateChange {
  object Loading : PartialStateChange
  data class Success(val users: List<UserItem>, val submittedQuery: String) : PartialStateChange
  data class Failure(val error: UserError, val submittedQuery: String) : PartialStateChange
  data class QueryChange(val query: String) : PartialStateChange

  fun reduce(state: ViewState): ViewState = when (this) {
    is Failure -> state.copy(
      isLoading = false,
      error = error,
      submittedQuery = submittedQuery,
      users = emptyList()
    )
    Loading -> state.copy(
      isLoading = true,
      error = null,
      users = emptyList()
    )
    is Success -> state.copy(
      isLoading = false,
      error = null,
      users = users,
      submittedQuery = submittedQuery,
    )
    is QueryChange -> {
      if (state.originalQuery == query) state
      else state.copy(originalQuery = query)
    }
  }
}

sealed interface SingleEvent : MviSingleEvent {
  data class SearchFailure(val error: UserError) : SingleEvent
}
