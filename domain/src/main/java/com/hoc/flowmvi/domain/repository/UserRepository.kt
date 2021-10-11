package com.hoc.flowmvi.domain.repository

import arrow.core.Either
import com.hoc.flowmvi.domain.entity.User
import kotlinx.coroutines.flow.Flow

sealed class UserError(message: String?, cause: Throwable?) : Exception(message, cause) {
  data class NetworkError(
    override val cause: Throwable?,
    override val message: String? = cause?.message,
  ) : UserError(message, cause)

  data class Unexpected(
    override val cause: Throwable?,
    override val message: String? = cause?.message,
  ) : UserError(message, cause)
}

interface UserRepository {
  fun getUsers(): Flow<Either<UserError, List<User>>>

  suspend fun refresh(): Either<UserError, Unit>

  suspend fun remove(user: User): Either<UserError, Unit>

  suspend fun add(user: User): Either<UserError, Unit>

  suspend fun search(query: String): Either<UserError, List<User>>
}
