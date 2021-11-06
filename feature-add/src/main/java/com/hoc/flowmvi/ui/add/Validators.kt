package com.hoc.flowmvi.ui.add

import androidx.core.util.PatternsCompat
import arrow.core.ValidatedNel
import arrow.core.validNel

private const val MIN_LENGTH_FIRST_NAME = 3
private const val MIN_LENGTH_LAST_NAME = 3

internal fun validateFirstName(firstName: String?): ValidatedNel<ValidationError, String> {
  if (firstName == null || firstName.length < MIN_LENGTH_FIRST_NAME) {
    return ValidationError.TOO_SHORT_FIRST_NAME.asInvalidNel
  }
  // more validations here
  return firstName.validNel()
}

internal fun validateLastName(lastName: String?): ValidatedNel<ValidationError, String> {
  if (lastName == null || lastName.length < MIN_LENGTH_LAST_NAME) {
    return ValidationError.TOO_SHORT_LAST_NAME.asInvalidNel
  }
  // more validations here
  return lastName.validNel()
}

internal fun validateEmail(email: String?): ValidatedNel<ValidationError, String> {
  if (email == null || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
    return ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel
  }
  // more validations here
  return email.validNel()
}
