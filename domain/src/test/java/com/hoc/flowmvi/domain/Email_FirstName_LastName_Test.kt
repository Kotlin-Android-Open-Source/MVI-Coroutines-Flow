package com.hoc.flowmvi.ui.add

import arrow.core.orNull
import com.hoc.flowmvi.domain.model.Email
import com.hoc.flowmvi.domain.model.FirstName
import com.hoc.flowmvi.domain.model.LastName
import com.hoc.flowmvi.domain.model.ValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create(null),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create(""),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      Email.create("a"),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
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
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create(null),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create(""),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      FirstName.create("a"),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
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
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create(null),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create(""),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create("a"),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      LastName.create("ab"),
    )
  }
}
