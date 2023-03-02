package com.hoc.flowmvi.data

import arrow.core.Either.Companion.catch as catchEither
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.leftWiden
import arrow.core.raise.either
import arrow.core.right
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.retryWithExponentialBackoff
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import timber.log.Timber

@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
internal class UserRepositoryImpl(
  private val userApiService: UserApiService,
  private val dispatchers: AppCoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, EitherNes<UserValidationError, User>>,
  private val domainToBody: Mapper<User, UserBody>,
  private val errorMapper: Mapper<Throwable, UserError>,
) : UserRepository {

  private sealed class Change {
    class Removed(val removed: User) : Change()
    class Refreshed(val user: List<User>) : Change()
    class Added(val user: User) : Change()
  }

  private val changesFlow = MutableSharedFlow<Change>(extraBufferCapacity = 64)
  private suspend inline fun sendChange(change: Change) = changesFlow.emit(change)

  private fun getUsersFromRemote(): Flow<List<User>> = flowFromSuspend {
    Timber.d("[USER_REPO] getUsersFromRemote ...")

    userApiService
      .getUsers()
      .map { response ->
        responseToDomain(response)
          .mapLeft(UserError::ValidationFailed)
          .onLeft { logError(it, "Map $response to user") }
          .getOrElse { throw it }
      }
  }
    .retryWithExponentialBackoff(
      maxAttempt = 2,
      initialDelay = 500.milliseconds,
      factor = 2.0,
    ) { it is IOException }

  override fun getUsers() = getUsersFromRemote()
    .flatMapConcat { initial ->
      changesFlow
        .onEach { Timber.d("[USER_REPO] Change=$it") }
        .scan(initial) { acc, change ->
          when (change) {
            is Change.Removed -> acc.filter { it.id != change.removed.id }
            is Change.Refreshed -> change.user
            is Change.Added -> acc + change.user
          }
        }
    }
    .onEach { Timber.d("[USER_REPO] Emit users.size=${it.size} ") }
    .map { it.right().leftWiden<UserError, Nothing, List<User>>() }
    .catch {
      logError(it, "getUsers")
      emit(errorMapper(it).left())
    }

  override suspend fun refresh() = catchEither { getUsersFromRemote().first() }
    .onRight { sendChange(Change.Refreshed(it)) }
    .map { }
    .onLeft { logError(it, "refresh") }
    .mapLeft(errorMapper)

  override suspend fun remove(user: User) = either {
    withContext(dispatchers.io) {
      val response = catchEither { userApiService.remove(user.id) }
        .onLeft { logError(it, "remove user=$user") }
        .mapLeft(errorMapper)
        .bind()

      val deleted = responseToDomain(response)
        .mapLeft { UserError.ValidationFailed(it) }
        .onLeft { logError(it, "remove user=$user") }
        .bind()

      sendChange(Change.Removed(deleted))
    }
  }

  override suspend fun add(user: User) = either {
    withContext(dispatchers.io) {
      val response = catchEither { userApiService.add(domainToBody(user)) }
        .onLeft { logError(it, "add user=$user") }
        .mapLeft(errorMapper)
        .bind()

      val added = responseToDomain(response)
        .mapLeft { UserError.ValidationFailed(it) }
        .onLeft { logError(it, "add user=$user") }
        .bind()

      sendChange(Change.Added(added))
    }
  }

  override suspend fun search(query: String) = either {
    withContext(dispatchers.io) {
      val userResponses = catchEither { userApiService.search(query) }
        .onLeft { logError(it, "search query=$query") }
        .mapLeft(errorMapper)
        .bind()

      val users = userResponses.map { userResponse ->
        responseToDomain(userResponse)
          .mapLeft(UserError::ValidationFailed)
          .onLeft { logError(it, "search query=$query") }
          .bind()
      }

      users
    }
  }

  private companion object {
    @Suppress("NOTHING_TO_INLINE")
    private inline fun logError(t: Throwable, message: String) = Timber.tag(TAG).e(t, message)

    private val TAG = UserRepositoryImpl::class.java.simpleName
  }
}
