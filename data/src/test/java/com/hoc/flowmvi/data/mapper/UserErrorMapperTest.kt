package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.NonEmptySet
import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException as KotlinCancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException as KotlinXCancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalStdlibApi
class UserErrorMapperTest {
  private val moshi = Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
  private val errorResponseJsonAdapter = moshi.adapter<ErrorResponse>()
  private val errorMapper = UserErrorMapper(errorResponseJsonAdapter)

  private fun buildHttpException(error: String, data: Any?) =
    HttpException(
      Response.error<Any>(
        400,
        errorResponseJsonAdapter.toJson(
          ErrorResponse(
            statusCode = 400,
            error = error,
            message = "error=$error",
            data = data,
          )
        ).toResponseBody("application/json".toMediaType())
      )
    )

  @Test
  fun test_withUserError_returnsItself() {
    assertEquals(UserError.NetworkError, errorMapper(UserError.NetworkError))
    assertEquals(UserError.UserNotFound("1"), errorMapper(UserError.UserNotFound("1")))
    assertEquals(UserError.InvalidId("1"), errorMapper(UserError.InvalidId("1")))
    assertEquals(
      UserError.ValidationFailed(NonEmptySet.of(UserValidationError.INVALID_EMAIL_ADDRESS)),
      errorMapper(UserError.ValidationFailed(NonEmptySet.of(UserValidationError.INVALID_EMAIL_ADDRESS))),
    )
    assertEquals(UserError.ServerError, errorMapper(UserError.ServerError))
    assertEquals(UserError.Unexpected, errorMapper(UserError.Unexpected))
  }

  @Test
  fun test_withFatalError_rethrows() {
    assertFailsWith<KotlinCancellationException> { errorMapper(KotlinCancellationException()) }
    assertFailsWith<KotlinXCancellationException> { errorMapper(KotlinXCancellationException()) }
  }

  @Test
  fun test_withIOException_returnsNetworkError() {
    assertEquals(
      UserError.NetworkError,
      errorMapper(IOException()),
    )
    assertEquals(
      UserError.NetworkError,
      errorMapper(UnknownHostException()),
    )
    assertEquals(
      UserError.NetworkError,
      errorMapper(SocketTimeoutException()),
    )
    assertEquals(
      UserError.NetworkError,
      errorMapper(SocketException()),
    )
    assertEquals(
      UserError.NetworkError,
      errorMapper(object : IOException() {}),
    )
  }

  @Test
  fun test_withHttpException_returnsUnexpectedError() {
    assertEquals(
      UserError.Unexpected,
      errorMapper(
        HttpException(
          Response.success(null)
        )
      ),
    )

    assertEquals(
      UserError.Unexpected,
      errorMapper(
        HttpException(
          Response.error<Any>(
            400,
            "{}".toResponseBody("application/json".toMediaType())
          )
        )
      ),
    )

    assertEquals(
      UserError.Unexpected,
      errorMapper(
        buildHttpException(
          "hello",
          mapOf(
            "1" to mapOf(
              "2" to 3,
              "3" to listOf("4", "5"),
              "6" to "7"
            ),
            "2" to null,
            "3" to listOf(
              hashMapOf("1" to "2"),
              hashMapOf("2" to "3"),
            )
          ),
        )
      ),
    )

    val id = mapOf("1" to "2")
    assertEquals(
      UserError.Unexpected,
      errorMapper(buildHttpException("invalid-id", id)),
    )
    assertEquals(
      UserError.Unexpected,
      errorMapper(buildHttpException("user-not-found", id)),
    )
  }

  @Test
  fun test_withHttpException_returnsCorrespondingUserError() {
    assertEquals(
      UserError.ServerError,
      errorMapper(buildHttpException("internal-error", null)),
    )

    val id = "id"
    assertEquals(
      UserError.InvalidId(id),
      errorMapper(buildHttpException("invalid-id", id)),
    )
    assertEquals(
      UserError.UserNotFound(id),
      errorMapper(buildHttpException("user-not-found", id)),
    )
    assertEquals(
      UserError.ValidationFailed(UserValidationError.VALUES_SET),
      errorMapper(buildHttpException("validation-failed", null)),
    )
  }

  @Test
  fun test_withOtherwiseExceptions_returnsUnexpectedError() {
    assertEquals(
      UserError.Unexpected,
      errorMapper(RuntimeException("Test 1")),
    )
    assertEquals(
      UserError.Unexpected,
      errorMapper(IndexOutOfBoundsException("Test 2")),
    )
  }
}
