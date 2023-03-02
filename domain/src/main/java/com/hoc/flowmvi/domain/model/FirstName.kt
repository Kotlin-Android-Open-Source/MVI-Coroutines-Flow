package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.EitherNes

@JvmInline
value class FirstName private constructor(val value: String) {
  companion object {
    fun create(value: String?): EitherNes<UserValidationError, FirstName> =
      validateFirstName(value).map(::FirstName)
  }
}
