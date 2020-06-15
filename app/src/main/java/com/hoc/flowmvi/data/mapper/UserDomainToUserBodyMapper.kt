package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.entity.User
import javax.inject.Inject

class UserDomainToUserBodyMapper @Inject constructor() : Mapper<User, UserBody> {
  override fun invoke(domain: User): UserBody {
    return UserBody(
        email = domain.email,
        avatar = domain.avatar,
        firstName = domain.firstName,
        lastName = domain.lastName
    )
  }
}