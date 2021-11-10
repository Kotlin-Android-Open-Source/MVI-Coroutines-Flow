package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel

@JvmInline
value class FirstName private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNel<ValidationError, FirstName> =
      validateFirstName(value).map(::FirstName)
  }
}
