package com.hoc.flowmvi.data

import arrow.core.Either
import arrow.core.left
import arrow.core.leftWiden
import arrow.core.right
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.retrySuspend
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
internal class UserRepositoryImpl(
  private val userApiService: UserApiService,
  private val dispatchers: CoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, User>,
  private val domainToBody: Mapper<User, UserBody>,
  private val errorMapper: Mapper<Throwable, UserError>,
) : UserRepository {

  private sealed class Change {
    class Removed(val removed: User) : Change()
    class Refreshed(val user: List<User>) : Change()
    class Added(val user: User) : Change()
  }

  private val changesFlow = MutableSharedFlow<Change>(extraBufferCapacity = 64)

  private suspend fun getUsersFromRemote(): List<User> {
    return withContext(dispatchers.io) {
      retrySuspend(
        times = 3,
        initialDelay = Duration.milliseconds(500),
        factor = 2.0,
        shouldRetry = { it is IOException }
      ) {
        Timber.d("[USER_REPO] Retry times=$it")
        userApiService.getUsers().map(responseToDomain)
      }
    }
  }

  override fun getUsers() = flow {
    val initial = getUsersFromRemote()

    changesFlow
      .onEach { Timber.d("[USER_REPO] Change=$it") }
      .scan(initial) { acc, change ->
        when (change) {
          is Change.Removed -> acc.filter { it.id != change.removed.id }
          is Change.Refreshed -> change.user
          is Change.Added -> acc + change.user
        }
      }
      .onEach { Timber.d("[USER_REPO] Emit users.size=${it.size} ") }
      .let { emitAll(it) }
  }
    .map { it.right().leftWiden<UserError, Nothing, List<User>>() }
    .catch {
      Timber.tag("UserRepositoryImpl").e(it, "getUsers")
      emit(errorMapper(it).left())
    }

  override suspend fun refresh() = Either.catch {
    getUsersFromRemote().let { changesFlow.emit(Change.Refreshed(it)) }
  }.tapLeft { Timber.tag("UserRepositoryImpl").e(it, "refresh") }
    .mapLeft(errorMapper)

  override suspend fun remove(user: User) = Either.catch {
    withContext(dispatchers.io) {
      val response = userApiService.remove(user.id)
      changesFlow.emit(Change.Removed(responseToDomain(response)))
    }
  }.tapLeft { Timber.tag("UserRepositoryImpl").e(it, "remove user=$user") }
    .mapLeft(errorMapper)

  override suspend fun add(user: User) = Either.catch {
    withContext(dispatchers.io) {
      val body = domainToBody(user)
      val response = userApiService.add(body)
      changesFlow.emit(Change.Added(responseToDomain(response)))
      extraDelay()
    }
  }.tapLeft { Timber.tag("UserRepositoryImpl").e(it, "add user=$user") }
    .mapLeft(errorMapper)

  override suspend fun search(query: String) = Either.catch {
    withContext(dispatchers.io) {
      extraDelay()
      userApiService.search(query).map(responseToDomain)
    }
  }.tapLeft { Timber.tag("UserRepositoryImpl").e(it, "search query=$query") }
    .mapLeft(errorMapper)

  private suspend inline fun extraDelay() = delay(400)
}
