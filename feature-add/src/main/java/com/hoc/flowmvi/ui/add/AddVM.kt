package com.hoc.flowmvi.ui.add

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.core.flatMapFirst
import com.hoc.flowmvi.core.withLatestFrom
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

@FlowPreview
@ExperimentalCoroutinesApi
internal class AddVM(private val addUser: AddUserUseCase) : ViewModel() {
  private val _eventFlow = MutableSharedFlow<SingleEvent>()
  private val _intentFlow = MutableSharedFlow<ViewIntent>()

  val viewState: StateFlow<ViewState>
  val singleEvent: Flow<SingleEvent> get() = _eventFlow

  suspend fun processIntent(intent: ViewIntent) = _intentFlow.emit(intent)

  init {
    val initialVS = ViewState.initial()
    viewState = _intentFlow
      .toPartialStateChangesFlow()
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .catch { Log.d("###", "[ADD_VM] Throwable: $it") }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)
  }

  private fun Flow<PartialStateChange>.sendSingleEvent(): Flow<PartialStateChange> {
    return onEach { change ->
      val event = when (change) {
        is PartialStateChange.ErrorsChanged -> return@onEach
        PartialStateChange.AddUser.Loading -> return@onEach
        is PartialStateChange.AddUser.AddUserSuccess -> SingleEvent.AddUserSuccess(change.user)
        is PartialStateChange.AddUser.AddUserFailure -> SingleEvent.AddUserFailure(
          change.user,
          change.throwable
        )
        PartialStateChange.FirstChange.EmailChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.FirstNameChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.LastNameChangedFirstTime -> return@onEach
      }
      _eventFlow.emit(event)
    }
  }

  private fun Flow<ViewIntent>.toPartialStateChangesFlow(): Flow<PartialStateChange> {
    val emailErrors = filterIsInstance<ViewIntent.EmailChanged>()
      .map { it.email }
      .map { validateEmail(it) to it }

    val firstNameErrors = filterIsInstance<ViewIntent.FirstNameChanged>()
      .map { it.firstName }
      .map { validateFirstName(it) to it }

    val lastNameErrors = filterIsInstance<ViewIntent.LastNameChanged>()
      .map { it.lastName }
      .map { validateLastName(it) to it }

    val userFormFlow =
      combine(emailErrors, firstNameErrors, lastNameErrors) { email, firstName, lastName ->
        UserForm(
          errors = email.first + firstName.first + lastName.first,
          user = User(
            firstName = firstName.second ?: "",
            email = email.second ?: "",
            lastName = lastName.second ?: "",
            id = "",
            avatar = ""
          )
        )
      }
        .shareIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed()
        )

    val addUserChanges = filterIsInstance<ViewIntent.Submit>()
      .withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .filter { it.errors.isEmpty() }
      .map { it.user }
      .flatMapFirst { user ->
        flow { emit(addUser(user)) }
          .map {
            @Suppress("USELESS_CAST")
            PartialStateChange.AddUser.AddUserSuccess(user) as PartialStateChange.AddUser
          }
          .onStart { emit(PartialStateChange.AddUser.Loading) }
          .catch { emit(PartialStateChange.AddUser.AddUserFailure(user, it)) }
      }

    val firstChanges = merge(
      filterIsInstance<ViewIntent.EmailChangedFirstTime>()
        .map { PartialStateChange.FirstChange.EmailChangedFirstTime },
      filterIsInstance<ViewIntent.FirstNameChangedFirstTime>()
        .map { PartialStateChange.FirstChange.FirstNameChangedFirstTime },
      filterIsInstance<ViewIntent.LastNameChangedFirstTime>()
        .map { PartialStateChange.FirstChange.LastNameChangedFirstTime }
    )

    return merge(
      userFormFlow
        .map { it.errors }
        .map { PartialStateChange.ErrorsChanged(it) },
      addUserChanges,
      firstChanges,
    )
  }

  private companion object {
    const val MIN_LENGTH_FIRST_NAME = 3
    const val MIN_LENGTH_LAST_NAME = 3

    private data class UserForm(
      val errors: Set<ValidationError>,
      val user: User
    )

    fun validateFirstName(firstName: String?): Set<ValidationError> {
      val errors = mutableSetOf<ValidationError>()

      if (firstName == null || firstName.length < MIN_LENGTH_FIRST_NAME) {
        errors += ValidationError.TOO_SHORT_FIRST_NAME
      }

      // more validation here

      return errors
    }

    fun validateLastName(lastName: String?): Set<ValidationError> {
      val errors = mutableSetOf<ValidationError>()

      if (lastName == null || lastName.length < MIN_LENGTH_LAST_NAME) {
        errors += ValidationError.TOO_SHORT_LAST_NAME
      }

      // more validation here

      return errors
    }

    fun validateEmail(email: String?): Set<ValidationError> {
      val errors = mutableSetOf<ValidationError>()

      if (email == null || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
        errors += ValidationError.INVALID_EMAIL_ADDRESS
      }

      // more validation here

      return errors
    }
  }
}
