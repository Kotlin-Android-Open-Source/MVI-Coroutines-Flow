package com.hoc.flowmvi.core_ui

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import timber.log.Timber

@OptIn(ExperimentalStdlibApi::class)
suspend inline fun debugCheckImmediateMainDispatcher() {
  if (BuildConfig.DEBUG) {
    val dispatcher = checkNotNull(currentCoroutineContext()[CoroutineDispatcher]) {
      "Expected CoroutineDispatcher in current CoroutineContext but was null"
    }.also { Timber.d("debugCheckImmediateMainDispatcher: dispatcher=$it") }

    check(
      dispatcher === Dispatchers.Main.immediate ||
        !dispatcher.isDispatchNeeded(Dispatchers.Main.immediate),
    ) {
      "Expected ContinuationInterceptor to be Dispatchers.Main.immediate but was $dispatcher"
    }
  }
}
