package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@FlowPreview
@ExperimentalCoroutinesApi
class UserRepositoryImpl(
  private val userApiService: UserApiService,
  private val dispatchers: CoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, User>,
  private val domainToResponse: Mapper<User, UserResponse>
) : UserRepository {
  private sealed class Change {
    data class Removed(val removed: User) : Change()
  }

  private val changesChannel = BroadcastChannel<Change>(Channel.CONFLATED)

  private suspend fun refreshInternal(): List<User> {
    return withContext(dispatchers.io) {
      userApiService.getUsers().map(responseToDomain)
    }
  }

  override fun getUsers(): Flow<List<User>> {
    return flow {
      val initial = refreshInternal()
      changesChannel
        .asFlow()
        .scan(initial) { acc, change ->
          when (change) {
            is Change.Removed -> acc.filter { it.id != change.removed.id }
          }
        }
        .let { emitAll(it) }
    }
  }

  override suspend fun refresh() {
    refreshInternal()
  }

  override suspend fun remove(user: User) {
    withContext(dispatchers.io) {
      val response = userApiService.remove(domainToResponse(user).id)
      changesChannel.send(Change.Removed(responseToDomain(response)))
    }
  }
}