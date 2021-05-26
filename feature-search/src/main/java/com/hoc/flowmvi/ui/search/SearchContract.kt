package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.domain.entity.User
import dev.ahmedmourad.nocopy.annotations.NoCopy

@NoCopy
internal data class UserItem private constructor(
  val id: String,
  val email: String,
  val avatar: String,
  val fullName: String,
) {
  companion object {
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
    data class Success(val users: List<UserItem>) : Search()
    data class Failure(val error: Throwable) : Search()

    override fun reduce(state: ViewState) = when (this) {
      is Failure -> state.copy(isLoading = false, error = error)
      Loading -> state.copy(isLoading = true, error = null)
      is Success -> state.copy(isLoading = false, error = null, users = users)
    }
  }
}

internal sealed interface SingleEvent {
  data class SearchFailure(val error: Throwable) : SingleEvent
}
