package com.hoc.flowmvi.test_utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class TestCoroutineDispatcherRule(val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
  TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(testCoroutineDispatcher)
    println("$this::starting $description")
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
    testCoroutineDispatcher.cleanupTestCoroutines()
    println("$this::finished $description")
  }
}
