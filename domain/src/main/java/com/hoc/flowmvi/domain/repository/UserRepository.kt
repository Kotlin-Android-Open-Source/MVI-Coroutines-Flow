package com.hoc.flowmvi.domain.repository

import arrow.core.Either
import arrow.core.continuations.EffectScope
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import kotlinx.coroutines.flow.Flow

interface UserRepository {
  fun getUsers(): Flow<Either<UserError, List<User>>>

  context(EffectScope<UserError>)
  suspend fun refresh()

  context(EffectScope<UserError>)
  suspend fun remove(user: User)

  context(EffectScope<UserError>)
  suspend fun add(user: User)

  context(EffectScope<UserError>)
  suspend fun search(query: String): List<User>
}
