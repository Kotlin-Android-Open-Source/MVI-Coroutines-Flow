package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.domain.entity.User

internal class UserDomainToUserBodyMapper : Mapper<User, UserBody> {
  override fun invoke(domain: User): UserBody {
    return UserBody(
      email = domain.email,
      avatar = domain.avatar,
      firstName = domain.firstName,
      lastName = domain.lastName
    )
  }
}
