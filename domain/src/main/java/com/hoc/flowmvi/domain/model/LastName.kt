package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.EitherNes

@JvmInline
value class LastName private constructor(val value: String) {
  companion object {
    fun create(value: String?): EitherNes<UserValidationError, LastName> =
      validateLastName(value).map(::LastName)
  }
}
