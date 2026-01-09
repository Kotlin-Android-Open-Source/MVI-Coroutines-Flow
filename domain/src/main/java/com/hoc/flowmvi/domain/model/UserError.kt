package com.hoc.flowmvi.domain.model

import arrow.core.NonEmptySet

sealed class UserError : RuntimeException() {
  data object NetworkError : UserError() {
    private fun readResolve(): Any = NetworkError
  }

  data class UserNotFound(
    val id: String,
  ) : UserError()

  data class InvalidId(
    val id: String,
  ) : UserError()

  data class ValidationFailed(
    val errors: NonEmptySet<UserValidationError>,
  ) : UserError()

  data object ServerError : UserError() {
    private fun readResolve(): Any = ServerError
  }

  data object Unexpected : UserError() {
    private fun readResolve(): Any = Unexpected
  }
}
