package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.domain.repository.UserError
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.coroutines.cancellation.CancellationException as KotlinCancellationException
import kotlinx.coroutines.CancellationException as KotlinXCancellationException

@ExperimentalStdlibApi
class UserErrorMapperTest {
  private val moshi = Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
  private val errorResponseJsonAdapter = moshi.adapter<ErrorResponse>()
  private val errorMapper = UserErrorMapper(errorResponseJsonAdapter)

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
        HttpException(
          Response.error<Any>(
            400,
            errorResponseJsonAdapter.toJson(
              ErrorResponse(
                statusCode = 400,
                error = "hello",
                message = "hello",
                data = mapOf(
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
            ).toResponseBody("application/json".toMediaType())
          )
        )
      ),
    )
  }
}
