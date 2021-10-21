package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.domain.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals

class UserDomainToUserBodyMapperTest {
  private val mapper = UserDomainToUserBodyMapper()

  @Test
  fun test_UserDomainToUserBodyMapper() {
    val body = mapper(
      User(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      )
    )

    assertEquals(
      UserBody(
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
      ),
      body
    )
  }
}
