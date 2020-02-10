package com.hoc.flowmvi.ui.add

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.*
import com.hoc.flowmvi.Event
import com.hoc.flowmvi.ui.add.AddContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class AddVM : ViewModel() {
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
      _eventD.value = when (change) {
        is PartialStateChange.ErrorsChanged -> return@onEach
      }
    }
  }

  private fun Flow<ViewIntent>.toPartialStateChangesFlow(): Flow<PartialStateChange> {
    val emailErrors = filterIsInstance<ViewIntent.EmailChanged>()
      .map { it.email }
      .map { validateEmail(it) }

    val firstNameErrors = filterIsInstance<ViewIntent.FirstNameChanged>()
      .map { it.firstName }
      .map { validateFirstName(it) }

    val lastNameErrors = filterIsInstance<ViewIntent.LastNameChanged>()
      .map { it.lastName }
      .map { validateLastName(it) }

    return combine(emailErrors, firstNameErrors, lastNameErrors) { s1, s2, s3 -> s1 + s2 + s3 }
      .map { PartialStateChange.ErrorsChanged(it) }
  }

  private companion object {
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


