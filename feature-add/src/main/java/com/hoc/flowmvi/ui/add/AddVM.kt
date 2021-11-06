package com.hoc.flowmvi.ui.add

import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import arrow.core.ValidatedNel
import arrow.core.orNull
import arrow.core.validNel
import arrow.core.zip
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.mapTo
import com.hoc081098.flowext.withLatestFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import timber.log.Timber

@ExperimentalCoroutinesApi
class AddVM(
  private val addUser: AddUserUseCase,
  private val savedStateHandle: SavedStateHandle,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = ViewState.initial(
      email = savedStateHandle.get<String?>(EMAIL_KEY),
      firstName = savedStateHandle.get<String?>(FIRST_NAME_KEY),
      lastName = savedStateHandle.get<String?>(LAST_NAME_KEY),
    )
    Timber.tag(logTag).d("[ADD_VM] initialVS: $initialVS")

    viewState = intentFlow
      .toPartialStateChangesFlow()
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .catch { Timber.tag(logTag).e(it, "[ADD_VM] Throwable: $it") }
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
          change.error
        )
        PartialStateChange.FirstChange.EmailChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.FirstNameChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.LastNameChangedFirstTime -> return@onEach
        is PartialStateChange.FormValueChange.EmailChanged -> {
          savedStateHandle[EMAIL_KEY] = change.email
          return@onEach
        }
        is PartialStateChange.FormValueChange.FirstNameChanged -> {
          savedStateHandle[FIRST_NAME_KEY] = change.firstName
          return@onEach
        }
        is PartialStateChange.FormValueChange.LastNameChanged -> {
          savedStateHandle[LAST_NAME_KEY] = change.lastName
          return@onEach
        }
      }
      sendEvent(event)
    }
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangesFlow(): Flow<PartialStateChange> {
    val emailFlow = filterIsInstance<ViewIntent.EmailChanged>()
      .log("Intent")
      .map { it.email }
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val firstNameFlow = filterIsInstance<ViewIntent.FirstNameChanged>()
      .log("Intent")
      .map { it.firstName }
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val lastNameFlow = filterIsInstance<ViewIntent.LastNameChanged>()
      .log("Intent")
      .map { it.lastName }
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val userFormFlow = combine(
      emailFlow.map { validateEmail(it) }.distinctUntilChanged(),
      firstNameFlow.map { validateFirstName(it) }.distinctUntilChanged(),
      lastNameFlow.map { validateLastName(it) }.distinctUntilChanged(),
    ) { emailValidated, firstNameValidated, lastNameValidated ->
      emailValidated.zip(firstNameValidated, lastNameValidated) { email, firstName, lastName ->
        User(
          firstName = firstName,
          email = email,
          lastName = lastName,
          id = "",
          avatar = ""
        )
      }
    }.stateWithInitialNullWhileSubscribed()

    val addUserChanges = filterIsInstance<ViewIntent.Submit>()
      .log("Intent")
      .withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .mapNotNull { it?.orNull() }
      .flatMapFirst { user ->
        flow { emit(addUser(user)) }
          .map { result ->
            result.fold(
              ifLeft = { PartialStateChange.AddUser.AddUserFailure(user, it) },
              ifRight = { PartialStateChange.AddUser.AddUserSuccess(user) }
            )
          }
          .onStart { emit(PartialStateChange.AddUser.Loading) }
      }

    val firstChanges = merge(
      filterIsInstance<ViewIntent.EmailChangedFirstTime>()
        .log("Intent")
        .take(1)
        .mapTo(PartialStateChange.FirstChange.EmailChangedFirstTime),
      filterIsInstance<ViewIntent.FirstNameChangedFirstTime>()
        .log("Intent")
        .take(1)
        .mapTo(PartialStateChange.FirstChange.FirstNameChangedFirstTime),
      filterIsInstance<ViewIntent.LastNameChangedFirstTime>()
        .log("Intent")
        .take(1)
        .mapTo(PartialStateChange.FirstChange.LastNameChangedFirstTime)
    )

    val formValuesChanges = merge(
      emailFlow.map { PartialStateChange.FormValueChange.EmailChanged(it) },
      firstNameFlow.map { PartialStateChange.FormValueChange.FirstNameChanged(it) },
      lastNameFlow.map { PartialStateChange.FormValueChange.LastNameChanged(it) },
    )

    val errorsChanges = userFormFlow.map { validated ->
      PartialStateChange.ErrorsChanged(
        validated?.fold(
          { it.toSet() },
          { emptySet() }
        ) ?: emptySet()
      )
    }

    return merge(
      formValuesChanges,
      errorsChanges,
      addUserChanges,
      firstChanges,
    )
  }

  private companion object {
    const val EMAIL_KEY = "com.hoc.flowmvi.ui.add.email"
    const val FIRST_NAME_KEY = "com.hoc.flowmvi.ui.add.first_name"
    const val LAST_NAME_KEY = "com.hoc.flowmvi.ui.add.last_name"

    const val MIN_LENGTH_FIRST_NAME = 3
    const val MIN_LENGTH_LAST_NAME = 3

    fun validateFirstName(firstName: String?): ValidatedNel<ValidationError, String> {
      if (firstName == null || firstName.length < MIN_LENGTH_FIRST_NAME) {
        return ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel
      }
      // more validations here
      return firstName.validNel()
    }

    fun validateLastName(lastName: String?): ValidatedNel<ValidationError, String> {
      if (lastName == null || lastName.length < MIN_LENGTH_LAST_NAME) {
        return ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel
      }
      // more validations here
      return lastName.validNel()
    }

    fun validateEmail(email: String?): ValidatedNel<ValidationError, String> {
      if (email == null || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
        return ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel
      }
      // more validations here
      return email.validNel()
    }
  }
}
