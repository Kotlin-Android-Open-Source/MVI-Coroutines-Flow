package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.domain.entity.User

internal enum class ValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME
}

internal data class ViewState(
  val errors: Set<ValidationError>,
  val isLoading: Boolean,
  //
  val emailChanged: Boolean,
  val firstNameChanged: Boolean,
  val lastNameChanged: Boolean,
  //
  val email: String?,
  val firstName: String?,
  val lastName: String?
) {
  companion object {
    fun initial(
      email: String?,
      firstName: String?,
      lastName: String?
    ) = ViewState(
      errors = emptySet(),

      isLoading = false,
      emailChanged = false,
      firstNameChanged = false,
      lastNameChanged = false,
      email = email,
      firstName = firstName,
      lastName = lastName,
    )
  }
}

internal sealed interface ViewIntent {
  data class EmailChanged(val email: String?) : ViewIntent
  data class FirstNameChanged(val firstName: String?) : ViewIntent
  data class LastNameChanged(val lastName: String?) : ViewIntent

  object Submit : ViewIntent

  object EmailChangedFirstTime : ViewIntent
  object FirstNameChangedFirstTime : ViewIntent
  object LastNameChangedFirstTime : ViewIntent
}

internal sealed interface PartialStateChange {
  fun reduce(viewState: ViewState): ViewState

  data class ErrorsChanged(val errors: Set<ValidationError>) : PartialStateChange {
    override fun reduce(viewState: ViewState) = viewState.copy(errors = errors)
  }

  sealed class AddUser : PartialStateChange {
    object Loading : AddUser()
    data class AddUserSuccess(val user: User) : AddUser()
    data class AddUserFailure(val user: User, val throwable: Throwable) : AddUser()

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        Loading -> viewState.copy(isLoading = true)
        is AddUserSuccess -> viewState.copy(isLoading = false)
        is AddUserFailure -> viewState.copy(isLoading = false)
      }
    }
  }

  sealed class FirstChange : PartialStateChange {
    object EmailChangedFirstTime : FirstChange()
    object FirstNameChangedFirstTime : FirstChange()
    object LastNameChangedFirstTime : FirstChange()

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        EmailChangedFirstTime -> viewState.copy(emailChanged = true)
        FirstNameChangedFirstTime -> viewState.copy(firstNameChanged = true)
        LastNameChangedFirstTime -> viewState.copy(lastNameChanged = true)
      }
    }
  }

  sealed class FormValueChange : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        is EmailChanged -> viewState.copy(email = email)
        is FirstNameChanged -> viewState.copy(firstName = firstName)
        is LastNameChanged -> viewState.copy(lastName = lastName)
      }
    }

    data class EmailChanged(val email: String?) : FormValueChange()
    data class FirstNameChanged(val firstName: String?) : FormValueChange()
    data class LastNameChanged(val lastName: String?) : FormValueChange()
  }
}

internal sealed interface SingleEvent {
  data class AddUserSuccess(val user: User) : SingleEvent
  data class AddUserFailure(val user: User, val throwable: Throwable) : SingleEvent
}
