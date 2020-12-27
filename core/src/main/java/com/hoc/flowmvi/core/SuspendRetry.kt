package com.hoc.flowmvi.core

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
suspend inline fun <T> retrySuspend(
  times: Int,
  initialDelay: Duration,
  factor: Double,
  maxDelay: Duration = Duration.INFINITE,
  shouldRetry: (Throwable) -> Boolean = { true },
  block: (times: Int) -> T,
): T {
  var currentDelay = initialDelay
  repeat(times - 1) {
    try {
      return block(it)
    } catch (e: Throwable) {
      if (!shouldRetry(e)) {
        throw e
      }
      // you can log an error here and/or make a more finer-grained
      // analysis of the cause to see if retry is needed
    }
    delay(currentDelay)
    currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
  }
  return block(times - 1) // last attempt
}
