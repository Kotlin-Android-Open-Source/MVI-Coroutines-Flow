package com.hoc.flowmvi.data.mapper

import arrow.core.nonFatalOrThrow
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.domain.repository.UserError
import com.squareup.moshi.JsonAdapter
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal class UserErrorMapper(private val errorResponseJsonAdapter: JsonAdapter<ErrorResponse>) :
  Mapper<Throwable, UserError> {
  override fun invoke(t: Throwable): UserError {
    t.nonFatalOrThrow()

    return runCatching {
      when (t) {
        is IOException -> when (t) {
          is UnknownHostException -> UserError.NetworkError
          is SocketTimeoutException -> UserError.NetworkError
          is SocketException -> UserError.NetworkError
          else -> UserError.NetworkError
        }
        is HttpException ->
          t.response()!!
            .takeUnless { it.isSuccessful }!!
            .errorBody()!!
            .use(ResponseBody::string)
            .let { mapResponseError(it) }
        else -> UserError.Unexpected
      }
    }.getOrElse {
      t.nonFatalOrThrow()
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
      "validation-failed" -> UserError.ValidationFailed
      else -> UserError.Unexpected
    }
  }
}
