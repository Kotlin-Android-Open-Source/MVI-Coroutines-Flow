package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.entity.User

class UserDomainToUserResponseMapper : Mapper<User, UserResponse> {
  override fun invoke(domain: User): UserResponse {
    return UserResponse(
      id = domain.id,
      avatar = domain.avatar,
      email = domain.email,
      firstName = domain.firstName,
      lastName = domain.lastName
    )
  }
}