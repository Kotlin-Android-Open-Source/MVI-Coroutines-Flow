package com.hoc.flowmvi.domain

import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.test_utils.leftValueOrThrow
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ID = "id"
private const val VALID_EMAIL = "hoc081098@gmail.com"
private const val VALID_NAME = "hoc081098"
private const val AVATAR = "avatar"

class UserTest {
  @Test
  fun testCreateUser_withValidValues_returnsValid() {
    val validated = User.create(
      id = ID,
      email = VALID_EMAIL,
      firstName = VALID_NAME,
      lastName = VALID_NAME,
      avatar = AVATAR,
    )
    assertTrue(validated.isRight())
    validated.rightValueOrThrow.let { user ->
      assertEquals(ID, user.id)
      assertEquals(VALID_EMAIL, user.email.value)
      assertEquals(VALID_NAME, user.firstName.value)
      assertEquals(VALID_NAME, user.lastName.value)
      assertEquals(AVATAR, user.avatar)
    }
  }

  @Test
  fun testCreateUser_withInvalidEmail_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = "invalid email",
      firstName = VALID_NAME,
      lastName = VALID_NAME,
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(UserValidationError.INVALID_EMAIL_ADDRESS, validated.leftValueOrThrow.single())
  }

  @Test
  fun testCreateUser_withInvalidFirstName_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = VALID_EMAIL,
      firstName = "h",
      lastName = VALID_NAME,
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(UserValidationError.TOO_SHORT_FIRST_NAME, validated.leftValueOrThrow.single())
  }

  @Test
  fun testCreateUser_withInvalidLastName_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = VALID_EMAIL,
      firstName = VALID_NAME,
      lastName = "h",
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(UserValidationError.TOO_SHORT_LAST_NAME, validated.leftValueOrThrow.single())
  }

  @Test
  fun testCreateUser_withInvalidEmailAndFirstName_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = "h",
      firstName = "h",
      lastName = VALID_NAME,
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(
      setOf(
        UserValidationError.INVALID_EMAIL_ADDRESS,
        UserValidationError.TOO_SHORT_FIRST_NAME,
      ),
      validated.leftValueOrThrow.toSet()
    )
  }

  @Test
  fun testCreateUser_withInvalidEmailAndLastName_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = "h",
      firstName = VALID_NAME,
      lastName = "h",
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(
      setOf(
        UserValidationError.INVALID_EMAIL_ADDRESS,
        UserValidationError.TOO_SHORT_LAST_NAME,
      ),
      validated.leftValueOrThrow.toSet()
    )
  }

  @Test
  fun testCreateUser_withInvalidFirstNameAndLastName_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = VALID_EMAIL,
      firstName = "h",
      lastName = "h",
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(
      setOf(
        UserValidationError.TOO_SHORT_FIRST_NAME,
        UserValidationError.TOO_SHORT_LAST_NAME,
      ),
      validated.leftValueOrThrow.toSet()
    )
  }

  @Test
  fun testCreateUser_withInvalidValues_returnsInvalid() {
    val validated = User.create(
      id = ID,
      email = "h",
      firstName = "h",
      lastName = "h",
      avatar = AVATAR,
    )
    assertTrue(validated.isLeft())
    assertEquals(
      UserValidationError.values().toSet(),
      validated.leftValueOrThrow.toSet()
    )
  }
}
