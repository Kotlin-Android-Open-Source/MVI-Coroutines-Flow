package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.entity.User
import javax.inject.Inject

class UserResponseToUserDomainMapper @Inject constructor() : Mapper<UserResponse, User> {
  override fun invoke(response: UserResponse): User {
    return User(
        id = response.id,
        avatar = response.avatar,
        email = response.email,
        firstName = response.firstName,
        lastName = response.lastName
    )
  }
}