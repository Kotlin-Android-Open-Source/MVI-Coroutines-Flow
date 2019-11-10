package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.User

class UserResponseToUserDomainMapper : Mapper<UserResponse, User> {
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