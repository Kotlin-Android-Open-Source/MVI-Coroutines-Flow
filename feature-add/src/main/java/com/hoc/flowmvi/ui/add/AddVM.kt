package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import arrow.core.continuations.effect
import arrow.core.orNull
import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.mapTo
import com.hoc081098.flowext.startWith
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import timber.log.Timber

@ExperimentalCoroutinesApi
class AddVM(
  private val addUser: AddUserUseCase,
  savedStateHandle: SavedStateHandle,
  appCoroutineDispatchers: AppCoroutineDispatchers,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>(appCoroutineDispatchers) {

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = savedStateHandle.get<ViewState?>(VIEW_STATE)?.copy(isLoading = false)
      ?: ViewState.initial()
    Timber.tag(logTag).d("[ADD_VM] initialVS: $initialVS")

    viewState = intentFlow
      .toPartialStateChangesFlow()
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .onEach { savedStateHandle[VIEW_STATE] = it }
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
        is PartialStateChange.FormValueChange.EmailChanged -> return@onEach
        is PartialStateChange.FormValueChange.FirstNameChanged -> return@onEach
        is PartialStateChange.FormValueChange.LastNameChanged -> return@onEach
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
      emailFlow,
      firstNameFlow,
      lastNameFlow,
    ) { email, firstName, lastName ->
      User.create(
        email = email,
        firstName = firstName,
        lastName = lastName,
        id = "",
        avatar = "",
      )
    }.stateWithInitialNullWhileSubscribed()

    val addUserChanges = filterIsInstance<ViewIntent.Submit>()
      .log("Intent")
      .withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .mapNotNull { it?.orNull() }
      .flatMapFirst { user ->
        flowOf(effect { addUser(user) })
          .map { result ->
            result.fold(
              recover = { PartialStateChange.AddUser.AddUserFailure(user, it) },
              transform = { PartialStateChange.AddUser.AddUserSuccess(user) }
            )
          }
          .startWith(PartialStateChange.AddUser.Loading)
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
    private const val VIEW_STATE = "com.hoc.flowmvi.ui.add.view_state"
  }
}
