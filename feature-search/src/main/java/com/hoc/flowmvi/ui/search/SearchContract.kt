package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import dev.ahmedmourad.nocopy.annotations.NoCopy

@Suppress("DataClassPrivateConstructor")
@NoCopy
data class UserItem private constructor(
  val id: String,
  val email: String,
  val avatar: String,
  val fullName: String,
) {
  companion object Factory {
    fun from(domain: User): UserItem {
      return UserItem(
        id = domain.id,
        email = domain.email,
        avatar = domain.avatar,
        fullName = "${domain.firstName} ${domain.lastName}",
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
  val query: String,
) : MviViewState {
  companion object Factory {
    fun initial(): ViewState {
      return ViewState(
        users = emptyList(),
        isLoading = false,
        error = null,
        query = "",
      )
    }
  }
}

internal sealed interface PartialStateChange {
  object Loading : PartialStateChange
  data class Success(val users: List<UserItem>, val query: String) : PartialStateChange
  data class Failure(val error: UserError, val query: String) : PartialStateChange

  fun reduce(state: ViewState) = when (this) {
    is Failure -> state.copy(
      isLoading = false,
      error = error,
      query = query,
      users = emptyList()
    )
    Loading -> state.copy(isLoading = true, error = null)
    is Success -> state.copy(
      isLoading = false,
      error = null,
      users = users,
      query = query,
    )
  }
}

sealed interface SingleEvent : MviSingleEvent {
  data class SearchFailure(val error: UserError) : SingleEvent
}
