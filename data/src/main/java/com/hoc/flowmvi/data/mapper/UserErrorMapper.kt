package com.hoc.flowmvi.data.mapper

import arrow.core.nonFatalOrThrow
import arrow.core.toNonEmptySetOrNull
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.squareup.moshi.JsonAdapter
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import okhttp3.ResponseBody
import retrofit2.HttpException

internal class UserErrorMapper(
  private val errorResponseJsonAdapter: JsonAdapter<ErrorResponse>,
) : Mapper<Throwable, UserError> {
  override fun invoke(throwable: Throwable): UserError {
    throwable.nonFatalOrThrow()

    return runCatching {
      when (throwable) {
        is UserError -> throwable
        is IOException ->
          when (throwable) {
            is UnknownHostException -> UserError.NetworkError
            is SocketTimeoutException -> UserError.NetworkError
            is SocketException -> UserError.NetworkError
            else -> UserError.NetworkError
          }
        is HttpException ->
          throwable
            .response()!!
            .takeUnless { it.isSuccessful }!!
            .errorBody()!!
            .use(ResponseBody::string)
            .let { mapResponseError(it) }
        else -> UserError.Unexpected
      }
    }.getOrElse {
      it.nonFatalOrThrow()
      UserError.Unexpected
    }
  }

  @Throws(Throwable::class)
  private fun mapResponseError(json: String): UserError {
    val errorResponse = errorResponseJsonAdapter.fromJson(json)!!

    return when (errorResponse.error) {
      "internal-error" -> UserError.ServerError
      "invalid-id" -> UserError.InvalidId(id = errorResponse.data as String)
      "user-not-found" -> UserError.UserNotFound(id = errorResponse.data as String)
      "validation-failed" ->
        UserError.ValidationFailed(
          errors = mapValidationErrors(errorResponse.data),
        )
      else -> UserError.Unexpected
    }
  }

  /**
   * Maps validation errors from server response data to UserValidationError set.
   * 
   * Expected data format can be:
   * - null: returns all validation errors
   * - List<String>: maps string values to corresponding UserValidationError enum values
   * - Map with "errors" key containing List<String>: maps the list values
   * 
   * String mappings:
   * - "invalid-email-address" or "INVALID_EMAIL_ADDRESS" -> UserValidationError.INVALID_EMAIL_ADDRESS
   * - "too-short-first-name" or "TOO_SHORT_FIRST_NAME" -> UserValidationError.TOO_SHORT_FIRST_NAME
   * - "too-short-last-name" or "TOO_SHORT_LAST_NAME" -> UserValidationError.TOO_SHORT_LAST_NAME
   */
  private fun mapValidationErrors(data: Any?): arrow.core.NonEmptySet<UserValidationError> {
    if (data == null) {
      // If no specific errors provided, return all validation errors
      return UserValidationError.VALUES_SET
    }

    val errorStrings = when (data) {
      is List<*> -> data.mapNotNull { it?.toString() }
      is Map<*, *> -> {
        // Try to extract errors from a map structure like {"errors": ["invalid-email-address"]}
        val errors = data["errors"]
        when (errors) {
          is List<*> -> errors.mapNotNull { it?.toString() }
          else -> emptyList()
        }
      }
      else -> emptyList()
    }

    val validationErrors = errorStrings.mapNotNull { errorString ->
      when (errorString.uppercase().replace("-", "_")) {
        "INVALID_EMAIL_ADDRESS" -> UserValidationError.INVALID_EMAIL_ADDRESS
        "TOO_SHORT_FIRST_NAME" -> UserValidationError.TOO_SHORT_FIRST_NAME
        "TOO_SHORT_LAST_NAME" -> UserValidationError.TOO_SHORT_LAST_NAME
        else -> null
      }
    }.toSet()

    // If we couldn't parse any valid errors, return all validation errors as fallback
    return validationErrors.toNonEmptySetOrNull() ?: UserValidationError.VALUES_SET
  }
}
