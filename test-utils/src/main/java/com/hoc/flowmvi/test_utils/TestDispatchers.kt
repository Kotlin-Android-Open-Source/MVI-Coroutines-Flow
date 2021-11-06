package com.hoc.flowmvi.test_utils

import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestDispatchers(testCoroutineDispatcher: TestCoroutineDispatcher) :
  CoroutineDispatchers {
  override val main: CoroutineDispatcher = testCoroutineDispatcher
  override val io: CoroutineDispatcher = testCoroutineDispatcher
}
