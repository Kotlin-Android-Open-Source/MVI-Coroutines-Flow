package com.hoc.flowmvi.core.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CoroutineDispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
}

class ImmediateDispatchersImpl : CoroutineDispatchers {
  override val main: CoroutineDispatcher = Dispatchers.Unconfined
  override val io: CoroutineDispatcher = Dispatchers.Unconfined
}
