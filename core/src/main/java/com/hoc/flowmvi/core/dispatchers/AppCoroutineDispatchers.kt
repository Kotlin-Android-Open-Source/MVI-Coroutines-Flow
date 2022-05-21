package com.hoc.flowmvi.core.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface AppCoroutineDispatchers {
  val main: CoroutineDispatcher
  val mainImmediate: CoroutineDispatcher
  val io: CoroutineDispatcher
}
