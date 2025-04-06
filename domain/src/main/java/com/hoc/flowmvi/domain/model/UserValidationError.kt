package com.hoc.flowmvi.domain.model

import arrow.core.NonEmptySet
import arrow.core.toNonEmptySetOrNull
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.leftNes

enum class UserValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME,
  ;

  val asLeftNes: EitherNes<UserValidationError, Nothing> = leftNes()

  companion object {
    val VALUES_SET: NonEmptySet<UserValidationError> = UserValidationError.entries.toNonEmptySetOrNull()!!
  }
}
