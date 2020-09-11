package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User

internal class UserResponseToUserDomainMapper : Mapper<UserResponse, User> {
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
