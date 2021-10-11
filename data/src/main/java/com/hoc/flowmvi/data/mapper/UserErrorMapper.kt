package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.data.remote.ErrorResponseJsonAdapter
import com.hoc.flowmvi.domain.repository.UserError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UserErrorMapper(private val errorResponseJsonAdapter: ErrorResponseJsonAdapter) :
  Mapper<Throwable, UserError> {
  override fun invoke(throwable: Throwable): UserError {
    throwable is UserError && return throwable

    return when (throwable) {
      is IOException -> {
        when (throwable) {
          is UnknownHostException -> UserError.NetworkError(
            throwable,
            "UnknownHostException: ${throwable.message}"
          )
          is SocketTimeoutException -> UserError.NetworkError(
            throwable,
            "SocketTimeoutException: ${throwable.message}"
          )
          is SocketException -> UserError.NetworkError(
            throwable,
            "SocketException: ${throwable.message}"
          )
          else -> UserError.NetworkError(
            throwable,
            "Unknown IOException: ${throwable.message}"
          )
        }
      }
      is HttpException -> {
        throwable.response()!!
          .takeUnless { it.isSuccessful }!!
          .errorBody()!!
          .use { body -> errorResponseJsonAdapter.fromJson(body.string())!! }
          .let(::mapResponseError)
      }
      else -> UserError.Unexpected(throwable)
    }
  }

  private fun mapResponseError(response: ErrorResponse): UserError {
    TODO()
  }
}
