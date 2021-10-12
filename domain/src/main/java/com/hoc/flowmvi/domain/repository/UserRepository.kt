package com.hoc.flowmvi.domain.repository

import arrow.core.Either
import com.hoc.flowmvi.domain.entity.User
import kotlinx.coroutines.flow.Flow

sealed interface UserError {
  object NetworkError : UserError
  data class UserNotFound(val id: String) : UserError
  data class InvalidId(val id: String) : UserError
  object ValidationFailed : UserError
  object ServerError : UserError
  object Unexpected : UserError
}

interface UserRepository {
  fun getUsers(): Flow<Either<UserError, List<User>>>

  suspend fun refresh(): Either<UserError, Unit>

  suspend fun remove(user: User): Either<UserError, Unit>

  suspend fun add(user: User): Either<UserError, Unit>

  suspend fun search(query: String): Either<UserError, List<User>>
}
