package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface AddContract {
  interface View {
    fun intents(): Flow<ViewIntent>
  }

  enum class ValidationError {
    INVALID_EMAIL_ADDRESS,
    TOO_SHORT_FIRST_NAME,
    TOO_SHORT_LAST_NAME
  }

  data class ViewState(
    val errors: Set<ValidationError>,
    val isLoading: Boolean
  ) {
    companion object {
      fun initial() = ViewState(
        errors = emptySet(),
        isLoading = false
      )
    }
  }

  sealed class ViewIntent {
    data class EmailChanged(val email: String?) : ViewIntent()
    data class FirstNameChanged(val firstName: String?) : ViewIntent()
    data class LastNameChanged(val lastName: String?) : ViewIntent()
    object Submit : ViewIntent()
  }

  sealed class PartialStateChange {
    abstract fun reduce(viewState: ViewState): ViewState

    data class ErrorsChanged(val errors: Set<ValidationError>) : PartialStateChange() {
      override fun reduce(viewState: ViewState) = viewState.copy(errors = errors)
    }

    sealed class AddUser : PartialStateChange() {
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
  }

  sealed class SingleEvent {
    data class AddUserSuccess(val user: User) : SingleEvent()
    data class AddUserFailure(val user: User, val throwable: Throwable) : SingleEvent()
  }
}