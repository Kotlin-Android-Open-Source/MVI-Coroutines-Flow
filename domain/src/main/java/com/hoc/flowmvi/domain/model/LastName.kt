package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.ValidatedNes

@JvmInline
value class LastName private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNes<UserValidationError, LastName> =
      validateLastName(value).map(::LastName)
  }
}
