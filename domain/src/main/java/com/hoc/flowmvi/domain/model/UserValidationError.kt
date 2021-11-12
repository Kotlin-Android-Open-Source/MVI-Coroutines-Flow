package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel
import arrow.core.invalidNel

enum class UserValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME;

  val asInvalidNel: ValidatedNel<UserValidationError, Nothing> = invalidNel()
}
