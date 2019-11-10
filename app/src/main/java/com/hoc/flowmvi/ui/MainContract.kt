package com.hoc.flowmvi.ui

import com.hoc.flowmvi.domain.User
import kotlinx.coroutines.flow.Flow


interface MainContract {
  interface View {
    fun intents(): Flow<ViewIntent>
  }

  data class UserItem(
    val id: Int,
    val email: String,
    val fullName: String,
    val avatar: String
  ) {
    constructor(domain: User) : this(
      id = domain.id,
      email = domain.email,
      fullName = "${domain.firstName} ${domain.lastName}",
      avatar = domain.avatar
    )
  }

  sealed class ViewIntent {
    object Initial : ViewIntent()
  }

  data class ViewState(
    val userItems: List<UserItem>,
    val isLoading: Boolean,
    val error: Throwable?
  ) {
    companion object {
      fun initial() = ViewState(
        userItems = emptyList(),
        isLoading = true,
        error = null
      )
    }
  }

  sealed class PartialChange {
    abstract fun reduce(vs: ViewState): ViewState

    sealed class GetUser : PartialChange() {
      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          Loading -> vs.copy(isLoading = true)
          is Data -> vs.copy(isLoading = false, error = null, userItems = users)
          is Error -> vs.copy(isLoading = false, error = error)
        }
      }

      object Loading : GetUser()
      data class Data(val users: List<UserItem>) : GetUser()
      data class Error(val error: Throwable) : GetUser()
    }
  }

  sealed class SingleEvent
}