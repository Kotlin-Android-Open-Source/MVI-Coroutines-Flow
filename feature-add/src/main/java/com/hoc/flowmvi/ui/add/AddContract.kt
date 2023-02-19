package com.hoc.flowmvi.ui.add

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import arrow.core.identity
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import com.hoc.flowmvi.mvi_base.MviViewStateSaver
import kotlinx.parcelize.Parcelize

@Parcelize
data class ViewState(
  val errors: Set<UserValidationError>,
  val isLoading: Boolean,
  // show error or not
  val emailChanged: Boolean,
  val firstNameChanged: Boolean,
  val lastNameChanged: Boolean,
  // form values
  val email: String,
  val firstName: String,
  val lastName: String,
) : MviViewState, Parcelable {
  companion object {
    private const val VIEW_STATE_KEY = "com.hoc.flowmvi.ui.add.StateSaver"

    fun initial() = ViewState(
      errors = UserValidationError.VALUES_SET,
      isLoading = false,
      emailChanged = false,
      firstNameChanged = false,
      lastNameChanged = false,
      email = "",
      firstName = "",
      lastName = "",
    )
  }

  class StateSaver : MviViewStateSaver<ViewState> {
    override fun ViewState.toBundle() = bundleOf(VIEW_STATE_KEY to this)

    override fun restore(bundle: Bundle?) = bundle
      ?.getParcelable<ViewState?>(VIEW_STATE_KEY)
      ?.copy(isLoading = false)
      ?: initial()
  }
}

sealed interface ViewIntent : MviIntent {
  data class EmailChanged(val email: String) : ViewIntent
  data class FirstNameChanged(val firstName: String) : ViewIntent
  data class LastNameChanged(val lastName: String) : ViewIntent

  object Submit : ViewIntent

  object EmailChangedFirstTime : ViewIntent
  object FirstNameChangedFirstTime : ViewIntent
  object LastNameChangedFirstTime : ViewIntent
}

internal sealed interface PartialStateChange {
  fun reduce(viewState: ViewState): ViewState

  data class UserFormState(
    val email: String,
    val firstName: String,
    val lastName: String,
    val userEitherNes: EitherNes<UserValidationError, User>,
  ) : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState = viewState.copy(
      email = email,
      firstName = firstName,
      lastName = lastName,
      errors = userEitherNes.fold(
        ifLeft = ::identity,
        ifRight = { emptySet() },
      ),
    )
  }

  sealed interface AddUser : PartialStateChange {
    object Loading : AddUser
    data class AddUserSuccess(val user: User) : AddUser
    data class AddUserFailure(val user: User, val error: UserError) : AddUser

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        Loading -> viewState.copy(isLoading = true)
        is AddUserSuccess -> viewState.copy(isLoading = false)
        is AddUserFailure -> viewState.copy(isLoading = false)
      }
    }
  }

  sealed interface FirstChange : PartialStateChange {
    object EmailChangedFirstTime : FirstChange
    object FirstNameChangedFirstTime : FirstChange
    object LastNameChangedFirstTime : FirstChange

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        EmailChangedFirstTime -> {
          if (viewState.emailChanged) viewState
          else viewState.copy(emailChanged = true)
        }
        FirstNameChangedFirstTime -> {
          if (viewState.firstNameChanged) viewState
          else viewState.copy(firstNameChanged = true)
        }
        LastNameChangedFirstTime -> {
          if (viewState.lastNameChanged) viewState
          else viewState.copy(lastNameChanged = true)
        }
      }
    }
  }
}

sealed interface SingleEvent : MviSingleEvent {
  data class AddUserSuccess(val user: User) : SingleEvent
  data class AddUserFailure(val user: User, val error: UserError) : SingleEvent
}
