package com.hoc.flowmvi.core.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CoroutineDispatchersImpl(
  override val main: CoroutineDispatcher = Dispatchers.Main,
  override val io: CoroutineDispatcher = Dispatchers.IO
) : CoroutineDispatchers
