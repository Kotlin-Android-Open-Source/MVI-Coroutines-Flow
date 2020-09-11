package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User

internal class UserDomainToUserResponseMapper : Mapper<User, UserResponse> {
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
