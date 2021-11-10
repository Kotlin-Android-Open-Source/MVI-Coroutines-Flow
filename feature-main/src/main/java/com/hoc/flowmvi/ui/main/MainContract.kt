package com.hoc.flowmvi.ui.main

import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState

data class UserItem(
  val id: String,
  val email: String,
  val avatar: String,
  val firstName: String,
  val lastName: String
) {
  val fullName get() = "$firstName $lastName"

  constructor(domain: User) : this(
    id = domain.id,
    email = domain.email.value,
    avatar = domain.avatar,
    firstName = domain.firstName.value,
    lastName = domain.lastName.value
  )

  fun toDomain() = User.create(
    id = id,
    lastName = lastName,
    firstName = firstName,
    avatar = avatar,
    email = email
  ).toEither().mapLeft(UserError::ValidationFailed)
}

sealed interface ViewIntent : MviIntent {
  object Initial : ViewIntent
  object Refresh : ViewIntent
  object Retry : ViewIntent
  data class RemoveUser(val user: UserItem) : ViewIntent
}

data class ViewState(
  val userItems: List<UserItem>,
  val isLoading: Boolean,
  val error: UserError?,
  val isRefreshing: Boolean
) : MviViewState {
  companion object {
    fun initial() = ViewState(
      userItems = emptyList(),
      isLoading = true,
      error = null,
      isRefreshing = false
    )
  }
}

internal sealed interface PartialChange {
  fun reduce(vs: ViewState): ViewState

  sealed class GetUser : PartialChange {
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
    data class Error(val error: UserError) : GetUser()
  }

  sealed class Refresh : PartialChange {
    override fun reduce(vs: ViewState): ViewState {
      return when (this) {
        is Success -> vs.copy(isRefreshing = false)
        is Failure -> vs.copy(isRefreshing = false)
        Loading -> vs.copy(isRefreshing = true)
      }
    }

    object Loading : Refresh()
    object Success : Refresh()
    data class Failure(val error: UserError) : Refresh()
  }

  sealed class RemoveUser : PartialChange {
    data class Success(val user: UserItem) : RemoveUser()
    data class Failure(val user: UserItem, val error: UserError) : RemoveUser()

    override fun reduce(vs: ViewState) = vs
  }
}

sealed interface SingleEvent : MviSingleEvent {
  sealed interface Refresh : SingleEvent {
    object Success : Refresh
    data class Failure(val error: UserError) : Refresh
  }

  data class GetUsersError(val error: UserError) : SingleEvent

  sealed interface RemoveUser : SingleEvent {
    data class Success(val user: UserItem) : RemoveUser
    data class Failure(
      val user: UserItem,
      val error: UserError,
      val indexProducer: () -> Int?,
    ) : RemoveUser
  }
}
