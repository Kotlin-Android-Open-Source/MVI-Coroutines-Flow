package com.hoc.flowmvi.data

import arrow.core.Either.Companion.catch as catchEither
import arrow.core.raise.either
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
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import timber.log.Timber

@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
internal class UserRepositoryImpl constructor(
  private val userApiService: UserApiService,
  private val dispatchers: AppCoroutineDispatchers,
  private val responseToDomain: Mapper<UserResponse, EitherNes<UserValidationError, User>>,
  private val domainToBody: Mapper<User, UserBody>,
  private val errorMapper: Mapper<Throwable, UserError>,
  private val userRepositoryStore: UserRepositoryStore,
) : UserRepository {
  override fun getUsers() =
    userRepositoryStore.observeUsersFlow()

  override suspend fun refresh() =
    userRepositoryStore.refresh()

  override suspend fun remove(user: User) =
    either {
      withContext(dispatchers.io) {
        val response =
          catchEither { userApiService.remove(user.id) }
            .onLeft { logError(it, "remove user=$user") }
            .mapLeft(errorMapper)
            .bind()

        val deleted =
          responseToDomain(response)
            .mapLeft { UserError.ValidationFailed(it) }
            .onLeft { logError(it, "remove user=$user") }
            .bind()

        userRepositoryStore.sendChange(UserRepositoryStore.Change.Removed(deleted))
      }
    }

  override suspend fun add(user: User) =
    either {
      withContext(dispatchers.io) {
        val response =
          catchEither { userApiService.add(domainToBody(user)) }
            .onLeft { logError(it, "add user=$user") }
            .mapLeft(errorMapper)
            .bind()

        val added =
          responseToDomain(response)
            .mapLeft { UserError.ValidationFailed(it) }
            .onLeft { logError(it, "add user=$user") }
            .bind()

        userRepositoryStore.sendChange(UserRepositoryStore.Change.Added(added))
      }
    }

  override suspend fun search(query: String) =
    either {
      withContext(dispatchers.io) {
        val userResponses =
          catchEither { userApiService.search(query) }
            .onLeft { logError(it, "search query=$query") }
            .mapLeft(errorMapper)
            .bind()

        val users =
          userResponses.map { userResponse ->
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
    private inline fun logError(
      t: Throwable,
      message: String,
    ) = Timber.tag(TAG).e(t, message)

    private val TAG = UserRepositoryImpl::class.java.simpleName
  }
}
