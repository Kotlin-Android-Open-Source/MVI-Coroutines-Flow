package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.mapTo
import com.hoc081098.flowext.startWith
import com.hoc081098.flowext.withLatestFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
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
  stateSaver: ViewState.StateSaver,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val rawLogTag get() = "AddVM[${System.identityHashCode(this)}]"

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = stateSaver.restore(savedStateHandle[VIEW_STATE_BUNDLE_KEY])
    Timber.tag(logTag).d("initialVS=$initialVS")

    viewState = intentSharedFlow
      .debugLog("ViewIntent")
      .toPartialStateChangeFlow(initialVS)
      .debugLog("PartialStateChange")
      .onEach { sendEvent(it.toSingleEventOrNull() ?: return@onEach) }
      .scan(initialVS) { state, change -> change.reduce(state) }
      .debugLog("ViewState")
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)

    savedStateHandle.setSavedStateProvider(VIEW_STATE_BUNDLE_KEY) {
      stateSaver.run { viewState.value.toBundle() }
    }
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(initialVS: ViewState): Flow<PartialStateChange> {
    val emailFlow = filterIsInstance<ViewIntent.EmailChanged>()
      .map { it.email }
      .startWith(initialVS.email)
      .distinctUntilChanged()

    val firstNameFlow = filterIsInstance<ViewIntent.FirstNameChanged>()
      .map { it.firstName }
      .startWith(initialVS.firstName)
      .distinctUntilChanged()

    val lastNameFlow = filterIsInstance<ViewIntent.LastNameChanged>()
      .map { it.lastName }
      .startWith(initialVS.lastName)
      .distinctUntilChanged()

    val userFormStateFlow = combine(
      emailFlow,
      firstNameFlow,
      lastNameFlow,
    ) { email, firstName, lastName ->
      PartialStateChange.UserFormState(
        email = email,
        firstName = firstName,
        lastName = lastName,
        userEitherNes = User.create(
          email = email,
          firstName = firstName,
          lastName = lastName,
          id = "",
          avatar = "",
        ),
      )
    }.shareWhileSubscribed()

    return merge(
      // user form state change
      userFormStateFlow,
      // first change
      toFirstChangeFlow(),
      // add user change
      filterIsInstance<ViewIntent.Submit>()
        .toAddUserChangeFlow(userFormStateFlow),
    )
  }

  //region Processors
  private fun SharedFlow<ViewIntent>.toFirstChangeFlow(): Flow<PartialStateChange.FirstChange> =
    merge(
      filterIsInstance<ViewIntent.EmailChangedFirstTime>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.EmailChangedFirstTime),
      filterIsInstance<ViewIntent.FirstNameChangedFirstTime>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.FirstNameChangedFirstTime),
      filterIsInstance<ViewIntent.LastNameChangedFirstTime>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.LastNameChangedFirstTime)
    )

  private fun Flow<ViewIntent.Submit>.toAddUserChangeFlow(userFormFlow: SharedFlow<PartialStateChange.UserFormState>): Flow<PartialStateChange.AddUser> =
    withLatestFrom(userFormFlow) { _, userForm -> userForm.userEitherNes }
      .debugLog("toAddUserChangeFlow::userValidatedNel")
      .mapNotNull { it.getOrNull() }
      .flatMapFirst { user ->
        flowFromSuspend { addUser(user) }
          .map { result ->
            result.fold(
              ifLeft = { PartialStateChange.AddUser.AddUserFailure(user, it) },
              ifRight = { PartialStateChange.AddUser.AddUserSuccess(user) }
            )
          }
          .startWith(PartialStateChange.AddUser.Loading)
      }
  //endregion

  private companion object {
    private const val VIEW_STATE_BUNDLE_KEY = "com.hoc.flowmvi.ui.add.view_state"

    private fun PartialStateChange.toSingleEventOrNull(): SingleEvent? = when (this) {
      is PartialStateChange.AddUser.AddUserSuccess -> SingleEvent.AddUserSuccess(user)
      is PartialStateChange.AddUser.AddUserFailure -> SingleEvent.AddUserFailure(
        user = user,
        error = error
      )
      PartialStateChange.FirstChange.EmailChangedFirstTime,
      PartialStateChange.FirstChange.FirstNameChangedFirstTime,
      PartialStateChange.FirstChange.LastNameChangedFirstTime,
      is PartialStateChange.UserFormState,
      PartialStateChange.AddUser.Loading,
      -> null
    }
  }
}
