package com.hoc.flowmvi.domain.model

import arrow.core.Either
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.rightNes
import com.hoc.flowmvi.core.zipOrAccumulateNonEmptySet

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
    ): EitherNes<UserValidationError, User> = Either.zipOrAccumulateNonEmptySet(
      Email.create(email),
      FirstName.create(firstName),
      LastName.create(lastName)
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

internal fun validateFirstName(firstName: String?): EitherNes<UserValidationError, String> {
  if (firstName == null || firstName.length < MIN_LENGTH_FIRST_NAME) {
    return UserValidationError.TOO_SHORT_FIRST_NAME.asLeftNes
  }
  // more validations here
  return firstName.rightNes()
}

internal fun validateLastName(lastName: String?): EitherNes<UserValidationError, String> {
  if (lastName == null || lastName.length < MIN_LENGTH_LAST_NAME) {
    return UserValidationError.TOO_SHORT_LAST_NAME.asLeftNes
  }
  // more validations here
  return lastName.rightNes()
}

internal fun validateEmail(email: String?): EitherNes<UserValidationError, String> {
  if (email == null || !EMAIL_ADDRESS_REGEX.matches(email)) {
    return UserValidationError.INVALID_EMAIL_ADDRESS.asLeftNes
  }
  // more validations here
  return email.rightNes()
}

private const val MIN_LENGTH_FIRST_NAME = 3
private const val MIN_LENGTH_LAST_NAME = 3
private val EMAIL_ADDRESS_REGEX =
  Regex("""[a-zA-Z0-9+._%\-']{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""")
