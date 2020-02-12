package com.hoc.flowmvi.ui.add

import androidx.core.util.PatternsCompat
import androidx.lifecycle.*
import com.hoc.flowmvi.Event
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.flatMapFirst
import com.hoc.flowmvi.ui.add.AddContract.*
import com.hoc.flowmvi.withLatestFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class AddVM(private val addUser: AddUserUseCase) : ViewModel() {
  private val initialVS = ViewState.initial()

  private val _viewStateD = MutableLiveData<ViewState>().apply { value = initialVS }
  val viewState: LiveData<ViewState> = _viewStateD.distinctUntilChanged()

  private val _eventD = MutableLiveData<Event<SingleEvent>>()
  val singleEvent: LiveData<Event<SingleEvent>> get() = _eventD

  private val _intentChannel = BroadcastChannel<ViewIntent>(capacity = Channel.BUFFERED)
  suspend fun processIntent(intent: ViewIntent) = _intentChannel.send(intent)

  init {
    _intentChannel
      .asFlow()
      .toPartialStateChangesFlow()
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .onEach { _viewStateD.value = it }
      .catch { }
      .launchIn(viewModelScope)
  }


  private fun Flow<PartialStateChange>.sendSingleEvent(): Flow<PartialStateChange> {
    return onEach { change ->
      _eventD.value = Event(
        when (change) {
          is PartialStateChange.ErrorsChanged -> return@onEach
          PartialStateChange.AddUser.Loading -> return@onEach
          is PartialStateChange.AddUser.AddUserSuccess -> SingleEvent.AddUserSuccess(change.user)
          is PartialStateChange.AddUser.AddUserFailure -> SingleEvent.AddUserFailure(
            change.user,
            change.throwable
          )
        }
      )
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

    val addUserChanges = filterIsInstance<ViewIntent.Submit>()
      .withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .filter { it.errors.isEmpty() }
      .map { it.user }
      .flatMapFirst { user ->
        flow { emit(addUser(user)) }
          .map { PartialStateChange.AddUser.AddUserSuccess(user) as PartialStateChange.AddUser }
          .onStart { emit(PartialStateChange.AddUser.Loading) }
          .catch { emit(PartialStateChange.AddUser.AddUserFailure(user, it)) }
      }

    return merge(
      userFormFlow
        .map { it.errors }
        .map { PartialStateChange.ErrorsChanged(it) },
      addUserChanges
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


