package com.hoc.flowmvi.ui.add

import arrow.core.validNel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ValidatorsTest {
  @Test
  fun testValidateEmail_withValidEmail_returnsValid() {
    assertEquals(
      "hoc081098@gmail.com".validNel(),
      validateEmail("hoc081098@gmail.com"),
    )
  }

  @Test
  fun testValidateEmail_withInvalidEmail_returnsInvalid() {
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      validateEmail(null),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      validateEmail(""),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      validateEmail("a"),
    )
    assertEquals(
      ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel,
      validateEmail("a@"),
    )
  }

  @Test
  fun testValidateFirstName_withValidFirstName_returnsValid() {
    assertEquals(
      "hoc081098".validNel(),
      validateFirstName("hoc081098"),
    )
  }

  @Test
  fun testValidateFirstName_withInvalidFirstName_returnsInvalid() {
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      validateFirstName(null),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      validateFirstName(""),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      validateFirstName("a"),
    )
    assertEquals(
      ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel,
      validateFirstName("ab"),
    )
  }

  @Test
  fun testValidateLastName_withValidLastName_returnsValid() {
    assertEquals(
      "hoc081098".validNel(),
      validateLastName("hoc081098"),
    )
  }

  @Test
  fun testValidateLastName_withInvalidLastName_returnsInvalid() {
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      validateLastName(null),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      validateLastName(""),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      validateLastName("a"),
    )
    assertEquals(
      ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel,
      validateLastName("ab"),
    )
  }
}
