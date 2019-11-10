package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.CoroutineDispatchers
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.User
import com.hoc.flowmvi.domain.UserRepository
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
  private val userApiService: UserApiService,
  private val mapper: Mapper<UserResponse, User>,
  private val dispatchers: CoroutineDispatchers
) : UserRepository {
  override suspend fun getUsers(): List<User> {
    return withContext(dispatchers.io) {
      userApiService.getUsers().map(mapper)
    }
  }
}