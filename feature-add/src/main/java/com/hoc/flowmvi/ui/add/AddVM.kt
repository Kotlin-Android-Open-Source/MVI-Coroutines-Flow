package com.hoc.flowmvi.ui.add

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.core.Either
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.withLatestFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

@ExperimentalCoroutinesApi
internal class AddVM(
  private val addUser: AddUserUseCase,
  private val savedStateHandle: SavedStateHandle
) : ViewModel() {
  private val _eventChannel = Channel<SingleEvent>(Channel.BUFFERED)
  private val _intentFlow = MutableSharedFlow<ViewIntent>(extraBufferCapacity = 64)

  val viewState: StateFlow<ViewState>
  val singleEvent: Flow<SingleEvent> get() = _eventChannel.receiveAsFlow()

  fun processIntent(intent: ViewIntent) = _intentFlow.tryEmit(intent)

  init {
    val initialVS = ViewState.initial(
      email = savedStateHandle.get<String?>(EMAIL_KEY),
      firstName = savedStateHandle.get<String?>(FIRST_NAME_KEY),
      lastName = savedStateHandle.get<String?>(LAST_NAME_KEY),
    )
    Log.d("###", "[ADD_VM] initialVS: $initialVS")

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
        is PartialStateChange.FormValueChange.EmailChanged -> return@onEach
        is PartialStateChange.FormValueChange.FirstNameChanged -> return@onEach
        is PartialStateChange.FormValueChange.LastNameChanged -> return@onEach
      }
      _eventChannel.send(event)
    }
  }

  private fun Flow<ViewIntent>.toPartialStateChangesFlow(): Flow<PartialStateChange> {
    val emailErrors = filterIsInstance<ViewIntent.EmailChanged>()
      .map { it.email }
      .distinctUntilChanged()
      .map { validateEmail(it) to it }
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed()
      )

    val firstNameErrors = filterIsInstance<ViewIntent.FirstNameChanged>()
      .map { it.firstName }
      .distinctUntilChanged()
      .map { validateFirstName(it) to it }
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed()
      )

    val lastNameErrors = filterIsInstance<ViewIntent.LastNameChanged>()
      .map { it.lastName }
      .distinctUntilChanged()
      .map { validateLastName(it) to it }
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed()
      )

    val userFormFlow =
      combine(emailErrors, firstNameErrors, lastNameErrors) { email, firstName, lastName ->
        val errors = email.first + firstName.first + lastName.first

        if (errors.isEmpty()) Either.Right(
          User(
            firstName = firstName.second!!,
            email = email.second!!,
            lastName = lastName.second!!,
            id = "",
            avatar = ""
          )
        ) else Either.Left(errors)
      }
        .shareIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed()
        )

    val addUserChanges = filterIsInstance<ViewIntent.Submit>()
      .withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .mapNotNull { it.rightOrNull() }
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

    val formValuesChanges = merge(
      emailErrors
        .map { it.second }
        .onEach { savedStateHandle.set(EMAIL_KEY, it) }
        .map { PartialStateChange.FormValueChange.EmailChanged(it) },
      firstNameErrors
        .map { it.second }
        .onEach { savedStateHandle.set(FIRST_NAME_KEY, it) }
        .map { PartialStateChange.FormValueChange.FirstNameChanged(it) },
      lastNameErrors
        .map { it.second }
        .onEach { savedStateHandle.set(LAST_NAME_KEY, it) }
        .map { PartialStateChange.FormValueChange.LastNameChanged(it) },
    )

    return merge(
      userFormFlow
        .map {
          PartialStateChange.ErrorsChanged(
            it.leftOrNull()
              ?: emptySet()
          )
        },
      addUserChanges,
      firstChanges,
      formValuesChanges,
    )
  }

  private companion object {
    const val EMAIL_KEY = "email"
    const val FIRST_NAME_KEY = "first_name"
    const val LAST_NAME_KEY = "last_name"

    const val MIN_LENGTH_FIRST_NAME = 3
    const val MIN_LENGTH_LAST_NAME = 3

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
