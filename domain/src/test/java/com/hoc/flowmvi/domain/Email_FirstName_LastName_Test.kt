package com.hoc.flowmvi.ui.add

import arrow.core.orNull
import com.hoc.flowmvi.domain.model.Email
import com.hoc.flowmvi.domain.model.FirstName
import com.hoc.flowmvi.domain.model.LastName
import com.hoc.flowmvi.domain.model.UserValidationError
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
    assertTrue(validated.isValid)
    assertEquals(
      "hoc081098@gmail.com",
      validated.orNull()?.value,
    )
  }

  @Test
  fun testCreateEmail_withInvalidEmail_returnsInvalid() {
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create(null),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create(""),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create("a"),
    )
    assertEquals(
      UserValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create("a@"),
    )
  }

  @Test
  fun testCreateFirstName_withValidFirstName_returnsValid() {
    val validated = FirstName.create("hoc081098")
    assertTrue(validated.isValid)
    assertEquals(
      "hoc081098",
      validated.orNull()?.value,
    )
  }

  @Test
  fun testCreateFirstName_withInvalidFirstName_returnsInvalid() {
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create(null),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create(""),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create("a"),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create("ab"),
    )
  }

  @Test
  fun testCreateLastName_withValidLastName_returnsValid() {
    val validated = LastName.create("hoc081098")
    assertTrue(validated.isValid)
    assertEquals(
      "hoc081098",
      validated.orNull()?.value,
    )
  }

  @Test
  fun testCreateLastName_withInvalidLastName_returnsInvalid() {
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create(null),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create(""),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create("a"),
    )
    assertEquals(
      UserValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create("ab"),
    )
  }
}
