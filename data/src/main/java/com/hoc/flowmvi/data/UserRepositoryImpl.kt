package com.hoc.flowmvi.data

import android.util.Log
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.retrySuspend
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
internal class UserRepositoryImpl constructor(
  private val userApiService: UserApiService,
  private val dispatchers: CoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, User>,
  private val domainToResponse: Mapper<User, UserResponse>,
  private val domainToBody: Mapper<User, UserBody>
) : UserRepository {

  private sealed class Change {
    data class Removed(val removed: User) : Change()
    data class Refreshed(val user: List<User>) : Change()
    class Added(val user: User) : Change()
  }

  private val changesFlow = MutableSharedFlow<Change>(extraBufferCapacity = 64)

  private suspend fun getUsersFromRemote(): List<User> {
    return withContext(dispatchers.io) {
      retrySuspend(
        times = 3,
        initialDelay = Duration.milliseconds(500),
        factor = 2.0,
      ) {
        Log.d("###", "[USER_REPO] Retry times=$it")
        userApiService.getUsers().map(responseToDomain)
      }
    }
  }

  override fun getUsers(): Flow<List<User>> {
    return flow {
      val initial = getUsersFromRemote()

      changesFlow
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
    getUsersFromRemote().let { changesFlow.emit(Change.Refreshed(it)) }

  override suspend fun remove(user: User) {
    withContext(dispatchers.io) {
      val response = userApiService.remove(domainToResponse(user).id)
      changesFlow.emit(Change.Removed(responseToDomain(response)))
    }
  }

  override suspend fun add(user: User) {
    withContext(dispatchers.io) {
      val body = domainToBody(user).copy(avatar = avatarUrls.random())
      val response = userApiService.add(body)
      changesFlow.emit(Change.Added(responseToDomain(response)))
      delay(400)
    }
  }

  override suspend fun search(query: String) = withContext(dispatchers.io) {
    delay(400)
    userApiService.search(query).map(responseToDomain)
  }

  companion object {
    private val avatarUrls =
      (0 until 100).map { "https://randomuser.me/api/portraits/men/$it.jpg" } +
        (0 until 100).map { "https://randomuser.me/api/portraits/women/$it.jpg" } +
        (0 until 10).map { "https://randomuser.me/api/portraits/lego/$it.jpg" }
  }
}
