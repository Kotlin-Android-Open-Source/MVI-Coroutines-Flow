package com.hoc.flowmvi.ui.main

import com.hoc.flowmvi.domain.entity.User

internal data class UserItem(
  val id: String,
  val email: String,
  val avatar: String,
  val firstName: String,
  val lastName: String
) {
  val fullName get() = "$firstName $lastName"

  constructor(domain: User) : this(
    id = domain.id,
    email = domain.email,
    avatar = domain.avatar,
    firstName = domain.firstName,
    lastName = domain.lastName
  )

  fun toDomain() = User(
    id = id,
    lastName = lastName,
    firstName = firstName,
    avatar = avatar,
    email = email
  )
}

internal sealed class ViewIntent {
  object Initial : ViewIntent()
  object Refresh : ViewIntent()
  object Retry : ViewIntent()
  data class RemoveUser(val user: UserItem) : ViewIntent()
}

internal data class ViewState(
  val userItems: List<UserItem>,
  val isLoading: Boolean,
  val error: Throwable?,
  val isRefreshing: Boolean,
  val shouldOpenNextScreen: Boolean
) : MviState {
  companion object {
    fun initial() = ViewState(
      userItems = emptyList(),
      isLoading = false,
      error = null,
      isRefreshing = false,
      shouldOpenNextScreen = false
    )
  }
}

internal sealed class PartialChange {

  sealed class GetUser : PartialChange() {
    object Loading : GetUser()
    data class Data(val users: List<UserItem>) : GetUser()
    data class Error(val error: Throwable) : GetUser()
  }

  sealed class Refresh : PartialChange() {

    object Loading : Refresh()
    object Success : Refresh()
    data class Failure(val error: Throwable) : Refresh()
  }

  sealed class RemoveUser : PartialChange() {
    data class Success(val user: UserItem) : RemoveUser()
    data class Failure(val user: UserItem, val error: Throwable) : RemoveUser()
  }
}
