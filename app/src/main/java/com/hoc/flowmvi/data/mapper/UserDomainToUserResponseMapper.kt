package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.entity.User
import javax.inject.Inject

class UserDomainToUserResponseMapper @Inject constructor() : Mapper<User, UserResponse> {
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