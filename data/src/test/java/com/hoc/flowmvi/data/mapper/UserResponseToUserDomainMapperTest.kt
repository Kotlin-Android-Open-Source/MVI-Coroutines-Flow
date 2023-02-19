package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.test_utils.leftValueOrThrow
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserResponseToUserDomainMapperTest {
  private val mapper = UserResponseToUserDomainMapper()

  @Test
  fun testUserDomainToUserResponseMapper_withValidResponse_returnsValid() {
    val validated = mapper(
      UserResponse(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      )
    )
    assertTrue(validated.isRight())
    assertEquals(
      User.create(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      ).rightValueOrThrow,
      validated.rightValueOrThrow,
    )
  }

  @Test
  fun testUserDomainToUserResponseMapper_withInvalidResponse_returnsInvalid() {
    val validated = mapper(
      UserResponse(
        id = "id",
        email = "email@",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      )
    )
    assertTrue(validated.isLeft())
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS,
      validated.leftValueOrThrow.single(),
    )
  }
}
