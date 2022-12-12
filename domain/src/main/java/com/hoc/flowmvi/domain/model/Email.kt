package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.ValidatedNes

@JvmInline
value class Email private constructor(val value: String) {
  companion object {
    fun create(value: String?): ValidatedNes<UserValidationError, Email> =
      validateEmail(value).map(::Email)
  }
}
