package com.hoc.flowmvi.data.mapper

import arrow.core.ValidatedNel
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError

internal class UserResponseToUserDomainMapper : Mapper<UserResponse, ValidatedNel<UserValidationError, User>> {
  override fun invoke(response: UserResponse): ValidatedNel<UserValidationError, User> {
    return User.create(
      id = response.id,
      avatar = response.avatar,
      email = response.email,
      firstName = response.firstName,
      lastName = response.lastName
    )
  }
}
