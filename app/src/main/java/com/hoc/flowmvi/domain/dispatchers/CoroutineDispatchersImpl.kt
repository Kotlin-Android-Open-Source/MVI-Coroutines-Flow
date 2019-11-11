package com.hoc.flowmvi.domain.dispatchers

import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CoroutineDispatchersImpl(
  override val main: CoroutineDispatcher = Dispatchers.Main,
  override val io: CoroutineDispatcher = Dispatchers.IO
) : CoroutineDispatchers