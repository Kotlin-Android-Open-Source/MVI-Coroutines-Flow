package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.NonEmptySet
import com.hoc.flowmvi.core.NonEmptySet.Companion.toNonEmptySetOrNull
import com.hoc.flowmvi.core.leftNes

enum class UserValidationError {
  INVALID_EMAIL_ADDRESS,
  TOO_SHORT_FIRST_NAME,
  TOO_SHORT_LAST_NAME;

  val asLeftNes: EitherNes<UserValidationError, Nothing> = leftNes()

  companion object {
    /**
     * Use this instead of [values()] for more performant.
     * See [KT-48872](https://youtrack.jetbrains.com/issue/KT-48872)
     */
    @JvmField
    val VALUES: List<UserValidationError> = values().asList()

    @JvmField
    val VALUES_SET: NonEmptySet<UserValidationError> = VALUES.toNonEmptySetOrNull()!!
  }
}
