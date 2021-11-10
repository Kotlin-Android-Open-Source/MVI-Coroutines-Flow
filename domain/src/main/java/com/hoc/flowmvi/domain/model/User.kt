package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel
import arrow.core.validNel
import arrow.core.zip

data class User(
  val id: String,
  val email: Email,
  val firstName: FirstName,
  val lastName: LastName,
  val avatar: String,
) {
  companion object {
    fun create(
      id: String,
      email: String?,
      firstName: String?,
      lastName: String?,
      avatar: String,
    ): ValidatedNel<ValidationError, User> = Email.create(email)
      .zip(
        FirstName.create(firstName),
        LastName.create(lastName),
      ) { e, f, l ->
        User(
          firstName = f,
          email = e,
          lastName = l,
          id = id,
          avatar = avatar
        )
      }
  }
}

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
  if (email == null || !EMAIL_ADDRESS.matches(email)) {
    return ValidationError.INVALID_EMAIL_ADDRESS.asInvalidNel
  }
  // more validations here
  return email.validNel()
}

private const val MIN_LENGTH_FIRST_NAME = 3
private const val MIN_LENGTH_LAST_NAME = 3
private val EMAIL_ADDRESS =
  Regex("""[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""")
