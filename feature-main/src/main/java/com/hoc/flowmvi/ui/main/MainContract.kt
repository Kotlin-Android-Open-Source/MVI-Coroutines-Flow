package com.hoc.flowmvi.ui.main

import androidx.annotation.MainThread
import arrow.core.Either
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import kotlin.LazyThreadSafetyMode.NONE

data class UserItem(
  val id: String,
  val email: String,
  val avatar: String,
  val firstName: String,
  val lastName: String
) {
  @get:MainThread
  val fullName by lazy(NONE) { "$firstName $lastName" }

  constructor(domain: User) : this(
    id = domain.id,
    email = domain.email.value,
    avatar = domain.avatar,
    firstName = domain.firstName.value,
    lastName = domain.lastName.value
  )

  fun toDomain(): Either<UserError.ValidationFailed, User> = User.create(
    id = id,
    lastName = lastName,
    firstName = firstName,
    avatar = avatar,
    email = email
  ).mapLeft { UserError.ValidationFailed(it) }
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
  inline val canRefresh get() = !isLoading && error === null

  companion object {
    fun initial() = ViewState(
      userItems = emptyList(),
      isLoading = true,
      error = null,
      isRefreshing = false
    )
  }
}

internal sealed interface PartialStateChange {
  fun reduce(viewState: ViewState): ViewState

  sealed interface Users : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        Loading -> viewState.copy(
          isLoading = true,
          error = null
        )
        is Data -> viewState.copy(
          isLoading = false,
          error = null,
          userItems = users
        )
        is Error -> viewState.copy(
          isLoading = false,
          error = error
        )
      }
    }

    object Loading : Users
    data class Data(val users: List<UserItem>) : Users
    data class Error(val error: UserError) : Users
  }

  sealed interface Refresh : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        is Success -> viewState.copy(isRefreshing = false)
        is Failure -> viewState.copy(isRefreshing = false)
        Loading -> viewState.copy(isRefreshing = true)
      }
    }

    object Loading : Refresh
    object Success : Refresh
    data class Failure(val error: UserError) : Refresh
  }

  sealed interface RemoveUser : PartialStateChange {
    data class Success(val user: UserItem) : RemoveUser
    data class Failure(val user: UserItem, val error: UserError) : RemoveUser

    override fun reduce(viewState: ViewState) = viewState
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
