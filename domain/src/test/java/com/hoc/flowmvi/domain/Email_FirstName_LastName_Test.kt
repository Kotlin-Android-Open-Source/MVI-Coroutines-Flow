package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.domain.model.Email
import com.hoc.flowmvi.domain.model.FirstName
import com.hoc.flowmvi.domain.model.LastName
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Suppress("ClassName")
class Email_FirstName_LastName_Test {
  @Test
  fun testCreateEmail_withValidEmail_returnsValid() {
    val validated = Email.create("hoc081098@gmail.com")
    assertTrue(validated.isRight())
    assertEquals(
      "hoc081098@gmail.com",
      validated.rightValueOrThrow.value,
    )
  }

  @Test
  fun testCreateEmail_withInvalidEmail_returnsInvalid() {
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asLeftNes,
      Email.create(null),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asLeftNes,
      Email.create(""),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asLeftNes,
      Email.create("a"),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asLeftNes,
      Email.create("a@"),
    )
  }

  @Test
  fun testCreateFirstName_withValidFirstName_returnsValid() {
    val validated = FirstName.create("hoc081098")
    assertTrue(validated.isRight())
    assertEquals(
      "hoc081098",
      validated.rightValueOrThrow.value,
    )
  }

  @Test
  fun testCreateFirstName_withInvalidFirstName_returnsInvalid() {
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asLeftNes,
      FirstName.create(null),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asLeftNes,
      FirstName.create(""),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asLeftNes,
      FirstName.create("a"),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asLeftNes,
      FirstName.create("ab"),
    )
  }

  @Test
  fun testCreateLastName_withValidLastName_returnsValid() {
    val validated = LastName.create("hoc081098")
    assertTrue(validated.isRight())
    assertEquals(
      "hoc081098",
      validated.rightValueOrThrow.value,
    )
  }

  @Test
  fun testCreateLastName_withInvalidLastName_returnsInvalid() {
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asLeftNes,
      LastName.create(null),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asLeftNes,
      LastName.create(""),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asLeftNes,
      LastName.create("a"),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asLeftNes,
      LastName.create("ab"),
    )
  }
}
