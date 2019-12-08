package com.hoc.flowmvi.ui

import com.hoc.flowmvi.domain.entity.User
import kotlinx.coroutines.flow.Flow


interface MainContract {
  interface View {
    fun intents(): Flow<ViewIntent>
  }

  data class UserItem(
    val id: String,
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
    object Refresh : ViewIntent()
    object Retry : ViewIntent()
  }

  data class ViewState(
    val userItems: List<UserItem>,
    val isLoading: Boolean,
    val error: Throwable?,
    val isRefreshing: Boolean
  ) {
    companion object {
      fun initial() = ViewState(
        userItems = emptyList(),
        isLoading = true,
        error = null,
        isRefreshing = false
      )
    }
  }

  sealed class PartialChange {
    abstract fun reduce(vs: ViewState): ViewState

    sealed class GetUser : PartialChange() {
      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          Loading -> vs.copy(
            isLoading = true,
            error = null
          )
          is Data -> vs.copy(
            isLoading = false,
            error = null,
            userItems = users
          )
          is Error -> vs.copy(
            isLoading = false,
            error = error
          )
        }
      }

      object Loading : GetUser()
      data class Data(val users: List<UserItem>) : GetUser()
      data class Error(val error: Throwable) : GetUser()
    }

    sealed class Refresh : PartialChange() {
      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          is Success -> vs.copy(isRefreshing = false)
          is Failure -> vs.copy(isRefreshing = false)
          Loading -> vs.copy(isRefreshing = true)
        }
      }

      object Loading : Refresh()
      object Success : Refresh()
      data class Failure(val error: Throwable) : Refresh()
    }
  }

  sealed class SingleEvent {
    sealed class Refresh : SingleEvent() {
      object Success : Refresh()
      data class Failure(val error: Throwable) : Refresh()
    }

    data class GetUsersError(val error: Throwable) : SingleEvent()
  }
}