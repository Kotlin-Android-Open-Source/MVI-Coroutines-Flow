package com.hoc.flowmvi.ui.add

import arrow.core.ValidatedNel
import arrow.core.invalidNel
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState

enum class ValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME;

  val asInvalidNel: ValidatedNel<ValidationError, Nothing> = invalidNel()
}

data class ViewState(
  val errors: Set<ValidationError>,
  val isLoading: Boolean,
  //
  val emailChanged: Boolean,
  val firstNameChanged: Boolean,
  val lastNameChanged: Boolean,
  //
  val email: String?,
  val firstName: String?,
  val lastName: String?,
) : MviViewState {
  companion object {
    fun initial(
      email: String?,
      firstName: String?,
      lastName: String?,
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

sealed interface ViewIntent : MviIntent {
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
    override fun reduce(viewState: ViewState) =
      if (viewState.errors == errors) viewState else viewState.copy(errors = errors)
  }

  sealed class AddUser : PartialStateChange {
    object Loading : AddUser()
    data class AddUserSuccess(val user: User) : AddUser()
    data class AddUserFailure(val user: User, val error: UserError) : AddUser()

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
        is EmailChanged -> {
          if (viewState.email == email) viewState
          else viewState.copy(email = email)
        }
        is FirstNameChanged -> {
          if (viewState.firstName == firstName) viewState
          else viewState.copy(firstName = firstName)
        }
        is LastNameChanged -> {
          if (viewState.lastName == lastName) viewState
          else viewState.copy(lastName = lastName)
        }
      }
    }

    data class EmailChanged(val email: String?) : FormValueChange()
    data class FirstNameChanged(val firstName: String?) : FormValueChange()
    data class LastNameChanged(val lastName: String?) : FormValueChange()
  }
}

sealed interface SingleEvent : MviSingleEvent {
  data class AddUserSuccess(val user: User) : SingleEvent
  data class AddUserFailure(val user: User, val error: UserError) : SingleEvent
}
