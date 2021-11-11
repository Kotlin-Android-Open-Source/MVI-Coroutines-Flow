package com.hoc.flowmvi.data

import arrow.core.ValidatedNel
import arrow.core.andThen
import arrow.core.computations.either
import arrow.core.left
import arrow.core.leftWiden
import arrow.core.right
import arrow.core.valueOr
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.retrySuspend
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
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
import arrow.core.Either.Companion.catch as catchEither

@ExperimentalTime
@ExperimentalCoroutinesApi
internal class UserRepositoryImpl(
  private val userApiService: UserApiService,
  private val dispatchers: CoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, ValidatedNel<UserValidationError, User>>,
  private val domainToBody: Mapper<User, UserBody>,
  private val errorMapper: Mapper<Throwable, UserError>,
) : UserRepository {

  private sealed class Change {
    class Removed(val removed: User) : Change()
    class Refreshed(val user: List<User>) : Change()
    class Added(val user: User) : Change()
  }

  private val responseToDomainThrows = responseToDomain andThen { validated ->
    validated.valueOr { throw UserError.ValidationFailed(it.toSet()) }
  }

  private val changesFlow = MutableSharedFlow<Change>(extraBufferCapacity = 64)

  private suspend inline fun sendChange(change: Change) = changesFlow.emit(change)

  @Suppress("NOTHING_TO_INLINE")
  private inline fun logError(t: Throwable, message: String) = Timber.tag(TAG).e(t, message)

  private suspend fun getUsersFromRemote(): List<User> {
    return withContext(dispatchers.io) {
      retrySuspend(
        times = 3,
        initialDelay = Duration.milliseconds(500),
        factor = 2.0,
        shouldRetry = { it is IOException }
      ) { times ->
        Timber.d("[USER_REPO] Retry times=$times")
        userApiService
          .getUsers()
          .map(responseToDomainThrows)
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
      logError(it, "getUsers")
      emit(errorMapper(it).left())
    }

  override suspend fun refresh() = catchEither { getUsersFromRemote() }
    .tap { sendChange(Change.Refreshed(it)) }
    .map { }
    .tapLeft { logError(it, "refresh") }
    .mapLeft(errorMapper)

  override suspend fun remove(user: User) = either<UserError, Unit> {
    withContext(dispatchers.io) {
      val response = catchEither { userApiService.remove(user.id) }
        .tapLeft { logError(it, "remove user=$user") }
        .mapLeft(errorMapper)
        .bind()

      val deleted = responseToDomain(response)
        .mapLeft { UserError.ValidationFailed(it.toSet()) }
        .tapInvalid { logError(it, "remove user=$user") }
        .bind()

      sendChange(Change.Removed(deleted))
    }
  }

  override suspend fun add(user: User) = either<UserError, Unit> {
    withContext(dispatchers.io) {
      val response = catchEither { userApiService.add(domainToBody(user)) }
        .tapLeft { logError(it, "add user=$user") }
        .mapLeft(errorMapper)
        .bind()

      delay(400) // TODO

      val added = responseToDomain(response)
        .mapLeft { UserError.ValidationFailed(it.toSet()) }
        .tapInvalid { logError(it, "add user=$user") }
        .bind()

      sendChange(Change.Added(added))
    }
  }

  override suspend fun search(query: String) = withContext(dispatchers.io) {
    catchEither { userApiService.search(query).map(responseToDomainThrows) }
      .tapLeft { logError(it, "search query=$query") }
      .mapLeft(errorMapper)
  }

  private companion object {
    private val TAG = UserRepositoryImpl::class.java.simpleName
  }
}
