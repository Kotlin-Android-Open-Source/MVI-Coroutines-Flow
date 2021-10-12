package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.domain.repository.UserError
import com.squareup.moshi.JsonAdapter
import io.mockk.mockk
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.coroutines.cancellation.CancellationException as KotlinCancellationException
import kotlinx.coroutines.CancellationException as KotlinXCancellationException

class UserErrorMapperTest {
  private val errorResponseJsonAdapter: JsonAdapter<ErrorResponse> = mockk()
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
}
