package com.hoc.flowmvi.domain.repository

import com.hoc.flowmvi.domain.entity.User

interface UserRepository {
  suspend fun getUsers(): List<User>
}