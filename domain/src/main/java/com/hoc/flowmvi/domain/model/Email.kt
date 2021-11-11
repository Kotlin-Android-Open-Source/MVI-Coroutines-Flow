package com.hoc.flowmvi.domain.model

import arrow.core.ValidatedNel

@JvmInline
value class Email private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNel<UserValidationError, Email> =
      validateEmail(value).map(::Email)
  }
}
