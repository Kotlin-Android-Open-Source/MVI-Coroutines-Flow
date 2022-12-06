package com.hoc.flowmvi.ui.main

import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
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

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = ViewState.initial()

    viewState = merge(
      intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
      intentFlow.filterNot { it is ViewIntent.Initial }
    )
      .toPartialChangeFlow()
      .sendSingleEvent()
      .scan(initialVS) { vs, change -> change.reduce(vs) }
      .catch { Timber.tag(logTag).e(it, "[MAIN_VM] Throwable: $it") }
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialVS
      )
  }

  private fun Flow<PartialChange>.sendSingleEvent(): Flow<PartialChange> {
    return onEach { change ->
      val event = when (change) {
        is PartialChange.GetUser.Error -> SingleEvent.GetUsersError(change.error)
        is PartialChange.Refresh.Success -> SingleEvent.Refresh.Success
        is PartialChange.Refresh.Failure -> SingleEvent.Refresh.Failure(change.error)
        is PartialChange.RemoveUser.Success -> SingleEvent.RemoveUser.Success(change.user)
        is PartialChange.RemoveUser.Failure -> SingleEvent.RemoveUser.Failure(
          user = change.user,
          error = change.error,
          indexProducer = {
            viewState.value
              .userItems
              .indexOfFirst { it.id == change.user.id }
              .takeIf { it != -1 }
          }
        )
        PartialChange.GetUser.Loading -> return@onEach
        is PartialChange.GetUser.Data -> return@onEach
        PartialChange.Refresh.Loading -> return@onEach
      }
      sendEvent(event)
    }
  }

  private fun Flow<ViewIntent>.toPartialChangeFlow(): Flow<PartialChange> =
    shareWhileSubscribed().run {
      val getUserChanges = defer(getUsersUseCase::invoke)
        .onEach { either -> Timber.d("[MAIN_VM] Emit users.size=${either.map { it.size }}") }
        .map { result ->
          result.fold(
            ifLeft = { PartialChange.GetUser.Error(it) },
            ifRight = { PartialChange.GetUser.Data(it.map(::UserItem)) }
          )
        }
        .startWith(PartialChange.GetUser.Loading)

      val refreshChanges = refreshGetUsers::invoke
        .asFlow()
        .map { result ->
          result.fold(
            ifLeft = { PartialChange.Refresh.Failure(it) },
            ifRight = { PartialChange.Refresh.Success }
          )
        }
        .startWith(PartialChange.Refresh.Loading)

      return merge(
        filterIsInstance<ViewIntent.Initial>()
          .log("Intent")
          .flatMapConcat { getUserChanges },
        filterIsInstance<ViewIntent.Refresh>()
          .filter { viewState.value.let { !it.isLoading && it.error === null } }
          .log("Intent")
          .flatMapFirst { refreshChanges },
        filterIsInstance<ViewIntent.Retry>()
          .filter { viewState.value.error != null }
          .log("Intent")
          .flatMapFirst { getUserChanges },
        filterIsInstance<ViewIntent.RemoveUser>()
          .log("Intent")
          .map { it.user }
          .flatMapMerge { userItem ->
            flowFromSuspend {
              userItem
                .toDomain()
                .flatMap { removeUser(it) }
            }
              .map { result ->
                result.fold(
                  ifLeft = { PartialChange.RemoveUser.Failure(userItem, it) },
                  ifRight = { PartialChange.RemoveUser.Success(userItem) },
                )
              }
          }
      )
    }
}
