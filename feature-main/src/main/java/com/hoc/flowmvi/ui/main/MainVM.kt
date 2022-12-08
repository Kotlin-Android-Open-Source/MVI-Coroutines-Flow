package com.hoc.flowmvi.ui.main

import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
import com.hoc.flowmvi.core.selfReferenced
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.defer
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class MainVM(
  private val getUsersUseCase: GetUsersUseCase,
  private val refreshGetUsers: RefreshGetUsersUseCase,
  private val removeUser: RemoveUserUseCase,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val rawLogTag get() = "MainVM[${System.identityHashCode(this)}]"

  override val viewState: StateFlow<ViewState> by selfReferenced {
    val initialVS = ViewState.initial()
    val getViewState = { viewState.value }

    intentSharedFlow
      .debugLog("ViewIntent")
      .filtered()
      .shareWhileSubscribed()
      .toPartialStateChangeFlow()
      .debugLog("PartialStateChange")
      .onEach { sendEvent(it.toSingleEventOrNull(getViewState) ?: return@onEach) }
      .scan(initialVS) { vs, change -> change.reduce(vs) }
      .debugLog("ViewState")
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialVS
      )
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(): Flow<PartialStateChange> = merge(
    // users change
    merge(
      filterIsInstance<ViewIntent.Initial>(),
      filterIsInstance<ViewIntent.Retry>()
        .filter { viewState.value.error != null },
    ).toUserChangeFlow(),
    // refresh change
    filterIsInstance<ViewIntent.Refresh>()
      .toRefreshChangeFlow(),
    // remove user change
    filterIsInstance<ViewIntent.RemoveUser>()
      .toRemoveUserChangeFlow()
  )

  //region Processors
  private fun Flow<ViewIntent>.toUserChangeFlow(): Flow<PartialStateChange.Users> {
    val userChanges = defer(getUsersUseCase::invoke)
      .onEach { either -> Timber.tag(logTag).d("Emit users.size=${either.map { it.size }}") }
      .map { result ->
        result.fold(
          ifLeft = { PartialStateChange.Users.Error(it) },
          ifRight = { PartialStateChange.Users.Data(it.map(::UserItem)) }
        )
      }
      .startWith(PartialStateChange.Users.Loading)

    return flatMapLatest { userChanges }
  }

  private fun Flow<ViewIntent.Refresh>.toRefreshChangeFlow(): Flow<PartialStateChange.Refresh> {
    val refreshChanges = flowFromSuspend(refreshGetUsers::invoke)
      .map { result ->
        result.fold(
          ifLeft = { PartialStateChange.Refresh.Failure(it) },
          ifRight = { PartialStateChange.Refresh.Success }
        )
      }
      .startWith(PartialStateChange.Refresh.Loading)

    return filter { viewState.value.canRefresh }
      .flatMapFirst { refreshChanges }
  }

  private fun Flow<ViewIntent.RemoveUser>.toRemoveUserChangeFlow(): Flow<PartialStateChange.RemoveUser> =
    map { it.user }
      .flatMapMerge { userItem ->
        flowFromSuspend {
          userItem
            .toDomain()
            .flatMap { removeUser(it) }
        }
          .map { result ->
            result.fold(
              ifLeft = { PartialStateChange.RemoveUser.Failure(userItem, it) },
              ifRight = { PartialStateChange.RemoveUser.Success(userItem) },
            )
          }
      }
  //endregion

  private companion object {
    private fun SharedFlow<ViewIntent>.filtered(): Flow<ViewIntent> = merge(
      filterIsInstance<ViewIntent.Initial>().take(1),
      filterNot { it is ViewIntent.Initial }
    )

    private fun PartialStateChange.toSingleEventOrNull(getViewState: () -> ViewState): SingleEvent? =
      when (this) {
        is PartialStateChange.Users.Error -> SingleEvent.GetUsersError(error)
        is PartialStateChange.Refresh.Success -> SingleEvent.Refresh.Success
        is PartialStateChange.Refresh.Failure -> SingleEvent.Refresh.Failure(error)
        is PartialStateChange.RemoveUser.Success -> SingleEvent.RemoveUser.Success(user)
        is PartialStateChange.RemoveUser.Failure -> SingleEvent.RemoveUser.Failure(
          user = user,
          error = error,
          indexProducer = {
            getViewState()
              .userItems
              .indexOfFirst { it.id == user.id }
              .takeIf { it != -1 }
          }
        )
        PartialStateChange.Users.Loading,
        is PartialStateChange.Users.Data,
        PartialStateChange.Refresh.Loading,
        -> null
      }
  }
}
