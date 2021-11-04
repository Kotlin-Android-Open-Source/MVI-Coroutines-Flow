package com.hoc.flowmvi.ui.main

import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
    return onEach {
      val event = when (it) {
        is PartialChange.GetUser.Error -> SingleEvent.GetUsersError(it.error)
        is PartialChange.Refresh.Success -> SingleEvent.Refresh.Success
        is PartialChange.Refresh.Failure -> SingleEvent.Refresh.Failure(it.error)
        is PartialChange.RemoveUser.Success -> SingleEvent.RemoveUser.Success(it.user)
        is PartialChange.RemoveUser.Failure -> SingleEvent.RemoveUser.Failure(
          user = it.user,
          error = it.error,
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
        .onStart { emit(PartialChange.GetUser.Loading) }

      val refreshChanges = refreshGetUsers::invoke
        .asFlow()
        .map { result ->
          result.fold(
            ifLeft = { PartialChange.Refresh.Failure(it) },
            ifRight = { PartialChange.Refresh.Success }
          )
        }
        .onStart { emit(PartialChange.Refresh.Loading) }

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
            flow {
              userItem
                .toDomain()
                .let { removeUser(it) }
                .let { emit(it) }
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

private fun <T> defer(flowFactory: () -> Flow<T>): Flow<T> = flow { emitAll(flowFactory()) }
