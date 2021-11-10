package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel

@JvmInline
value class LastName private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNel<ValidationError, LastName> =
      validateLastName(value).map(::LastName)
  }
}
