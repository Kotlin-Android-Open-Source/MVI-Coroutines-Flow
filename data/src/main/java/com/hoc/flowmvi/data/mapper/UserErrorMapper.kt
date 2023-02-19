package com.hoc.flowmvi.data.mapper

import arrow.core.nonFatalOrThrow
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

internal class UserErrorMapper(private val errorResponseJsonAdapter: JsonAdapter<ErrorResponse>) :
  Mapper<Throwable, UserError> {
  override fun invoke(throwable: Throwable): UserError {
    throwable.nonFatalOrThrow()

    return runCatching {
      when (throwable) {
        is UserError -> throwable
        is IOException -> when (throwable) {
          is UnknownHostException -> UserError.NetworkError
          is SocketTimeoutException -> UserError.NetworkError
          is SocketException -> UserError.NetworkError
          else -> UserError.NetworkError
        }
        is HttpException ->
          throwable.response()!!
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
      "validation-failed" -> UserError.ValidationFailed(
        errors = UserValidationError.VALUES_SET // TODO(hoc081098): Map validation errors from server response
      )
      else -> UserError.Unexpected
    }
  }
}
