package com.hoc.flowmvi.domain.model

import com.hoc.flowmvi.core.EitherNes

@JvmInline
value class Email private constructor(val value: String) {
  companion object {
    fun create(value: String?): EitherNes<UserValidationError, Email> =
      validateEmail(value).map(::Email)
  }
}
