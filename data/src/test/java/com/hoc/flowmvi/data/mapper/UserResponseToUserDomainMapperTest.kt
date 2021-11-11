package com.hoc.flowmvi.data.mapper

import arrow.core.identity
import arrow.core.orNull
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    assertTrue(validated.isValid)
    assertEquals(
      User.create(
        id = "id",
        email = "email@gmail.com",
        firstName = "first",
        lastName = "last",
        avatar = "avatar",
      ).orNull()!!,
      validated.orNull()!!,
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
    assertTrue(validated.isInvalid)
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS,
      assertNotNull(validated.fold(fe = ::identity, fa = { null })).head,
    )
  }
}
