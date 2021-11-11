package com.hoc.flowmvi.domain.model

sealed class UserError : Throwable() {
  object NetworkError : UserError()
  data class UserNotFound(val id: String) : UserError()
  data class InvalidId(val id: String) : UserError()
  data class ValidationFailed(val errors: Set<UserValidationError>) : UserError()
  object ServerError : UserError()
  object Unexpected : UserError()
}
