package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel
import arrow.core.invalidNel

enum class UserValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME;

  val asInvalidNel: ValidatedNel<UserValidationError, Nothing> = invalidNel()

  companion object {
    /**
     * Use this instead of [values()] for more performant.
     * See [KT-48872](https://youtrack.jetbrains.com/issue/KT-48872)
     */
    val VALUES: List<UserValidationError> = values().asList()

    val VALUES_SET: Set<UserValidationError> = VALUES.toSet()
  }
}
