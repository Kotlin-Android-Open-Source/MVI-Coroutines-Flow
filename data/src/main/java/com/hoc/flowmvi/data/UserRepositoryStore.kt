package com.hoc.flowmvi.data

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.Mapper
import arrow.core.Either.Companion.catch as catchEither
import com.hoc.flowmvi.data.UserRepositoryStore.Change
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc081098.flowext.FlowExtPreview
import com.hoc081098.flowext.catchAndReturn
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.retryWithExponentialBackoff
import com.hoc081098.flowext.scanWith
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

internal interface UserRepositoryStore {
  sealed interface Change {
    class Removed(val removed: User) : Change

    class Refreshed(val user: List<User>) : Change

    class Added(val user: User) : Change
  }

  interface Fetcher {
    suspend fun fetch(): List<User>
  }

  val errorMapper: Mapper<Throwable, UserError>
  val fetcher: Fetcher

  suspend fun sendChange(change: Change)
  suspend fun refresh(): Either<UserError, Unit>
  fun observeUsersFlow(): Flow<Either<UserError, List<User>>>
}

internal class DefaultUserRepositoryStore(
  override val errorMapper: Mapper<Throwable, UserError>,
  override val fetcher: UserRepositoryStore.Fetcher,
) : UserRepositoryStore {
  private val changesFlow = MutableSharedFlow<Change>(extraBufferCapacity = 64)

  override suspend fun sendChange(change: Change) =
    changesFlow.emit(change)

  @OptIn(FlowExtPreview::class)
  override fun observeUsersFlow() =
    changesFlow
      .onEach { Timber.tag(TAG).d("Change=$it") }
      .scanWith(fetcher::fetch) { acc, change ->
        when (change) {
          is Change.Removed -> acc.filter { it.id != change.removed.id }
          is Change.Refreshed -> change.user
          is Change.Added -> acc + change.user
        }
      }
      .onEach { Timber.tag(TAG).d("Emit users.size=${it.size} ") }
      .map { it.right() }
      .catchAndReturn {
        Timber.tag(TAG).e(it, "getUsers")
        errorMapper(it).left()
      }

  override suspend fun refresh() =
    catchEither { fetcher.fetch() }
      .onRight { sendChange(Change.Refreshed(it)) }
      .map {}
      .onLeft { Timber.tag(TAG).e(it, "refresh") }
      .mapLeft(errorMapper)

  private companion object {
    private val TAG = DefaultUserRepositoryStore::class.java.simpleName
  }
}


internal class UserFetcherWithExponentialBackoffRetry(
  private val userApiService: UserApiService,
  private val responseToDomain: Mapper<UserResponse, EitherNes<UserValidationError, User>>,
) : UserRepositoryStore.Fetcher {
  override suspend fun fetch(): List<User> =
    flowFromSuspend {
      Timber.d("[USER_REPO] getUsersFromRemote ...")

      userApiService
        .getUsers()
        .map { response ->
          responseToDomain(response)
            .mapLeft(UserError::ValidationFailed)
            .onLeft { Timber.tag(TAG).d(it, "Map $response to user") }
            .getOrElse { throw it }
        }
    }.retryWithExponentialBackoff(
      maxAttempt = 2,
      initialDelay = 500.milliseconds,
      factor = 2.0,
    ) { it is IOException }
      .first()

  private companion object {
    private val TAG = UserFetcherWithExponentialBackoffRetry::class.java.simpleName
  }
}
