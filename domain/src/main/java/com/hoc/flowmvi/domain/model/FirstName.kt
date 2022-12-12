package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.ValidatedNes

@JvmInline
value class FirstName private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNes<UserValidationError, FirstName> =
      validateFirstName(value).map(::FirstName)
  }
}
