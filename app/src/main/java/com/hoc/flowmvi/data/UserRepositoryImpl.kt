package com.hoc.flowmvi.data

import android.util.Log
import com.hoc.flowmvi.data.mapper.UserDomainToUserBodyMapper
import com.hoc.flowmvi.data.mapper.UserDomainToUserResponseMapper
import com.hoc.flowmvi.data.mapper.UserResponseToUserDomainMapper
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val dispatchers: CoroutineDispatchers,
    private val responseToDomain: UserResponseToUserDomainMapper,
    private val domainToResponse: UserDomainToUserResponseMapper,
    private val domainToBody: UserDomainToUserBodyMapper,
) : UserRepository {

  private sealed class Change {
    data class Removed(val removed: User) : Change()
    data class Refreshed(val user: List<User>) : Change()
    class Added(val user: User) : Change()
  }

  private val changesChannel = BroadcastChannel<Change>(Channel.CONFLATED)

  private suspend fun getUsersFromRemote(): List<User> {
    return withContext(dispatchers.io) {
      userApiService.getUsers().map(responseToDomain)
    }
  }

  @FlowPreview
  override fun getUsers(): Flow<List<User>> {
    return flow {
      val initial = getUsersFromRemote()

      changesChannel
          .asFlow()
          .onEach { Log.d("###", "[USER_REPO] Change=$it") }
          .scan(initial) { acc, change ->
            when (change) {
              is Change.Removed -> acc.filter { it.id != change.removed.id }
              is Change.Refreshed -> change.user
              is Change.Added -> acc + change.user
            }
          }
          .onEach { Log.d("###", "[USER_REPO] Emit users.size=${it.size} ") }
          .let { emitAll(it) }
    }
  }

  override suspend fun refresh() =
      getUsersFromRemote().let { changesChannel.send(Change.Refreshed(it)) }

  override suspend fun remove(user: User) {
    withContext(dispatchers.io) {
      val response = userApiService.remove(domainToResponse(user).id)
      changesChannel.send(Change.Removed(responseToDomain(response)))
    }
  }

  override suspend fun add(user: User) {
    withContext(dispatchers.io) {
      val body = domainToBody(user).copy(avatar = avatarUrls.random())
      val response = userApiService.add(body)
      changesChannel.send(Change.Added(responseToDomain(response)))
      delay(400)
    }
  }

  companion object {
    private val avatarUrls =
        (0 until 100).map { "https://randomuser.me/api/portraits/men/$it.jpg" } +
            (0 until 100).map { "https://randomuser.me/api/portraits/women/$it.jpg" } +
            (0 until 10).map { "https://randomuser.me/api/portraits/lego/$it.jpg" }
  }
}