package com.hoc.flowmvi.domain.repository

import com.hoc.flowmvi.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
  fun getUsers(): Flow<List<User>>

  suspend fun refresh()

  suspend fun remove(user: User)

  suspend fun add(user: User)
}
