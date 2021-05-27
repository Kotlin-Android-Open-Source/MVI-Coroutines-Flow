package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.domain.entity.User
import dev.ahmedmourad.nocopy.annotations.NoCopy

@Suppress("DataClassPrivateConstructor")
@NoCopy
internal data class UserItem private constructor(
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

internal sealed interface ViewIntent {
  data class Search(val query: String) : ViewIntent
  object Retry : ViewIntent
}

internal data class ViewState(
  val users: List<UserItem>,
  val isLoading: Boolean,
  val error: Throwable?,
  val query: String,
) {
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
  data class Failure(val error: Throwable, val query: String) : PartialStateChange

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

internal sealed interface SingleEvent {
  data class SearchFailure(val error: Throwable) : SingleEvent
}
