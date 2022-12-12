package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.NonEmptySet
import com.hoc.flowmvi.core.NonEmptySet.Companion.toNonEmptySetOrNull
import com.hoc.flowmvi.core.ValidatedNes
import com.hoc.flowmvi.core.invalidNes

enum class UserValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME;

  val asInvalidNes: ValidatedNes<UserValidationError, Nothing> = invalidNes()

  companion object {
    /**
     * Use this instead of [values()] for more performant.
     * See [KT-48872](https://youtrack.jetbrains.com/issue/KT-48872)
     */
    val VALUES: List<UserValidationError> = values().asList()

    val VALUES_SET: NonEmptySet<UserValidationError> = VALUES.toNonEmptySetOrNull()!!
  }
}
