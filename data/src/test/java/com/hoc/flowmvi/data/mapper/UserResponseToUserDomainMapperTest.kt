package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import kotlin.test.Test
import kotlin.test.assertEquals

class UserResponseToUserDomainMapperTest {
  private val mapper = UserResponseToUserDomainMapper()

  @Test
  fun test_UserDomainToUserResponseMapper() {
    val domain = mapper(
      UserResponse(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      )
    )

    assertEquals(
      User(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      ),
      domain
    )
  }
}
