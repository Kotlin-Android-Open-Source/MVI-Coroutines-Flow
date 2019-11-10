package com.hoc.flowmvi.domain

interface UserRepository {
  suspend fun getUsers(): List<User>
}