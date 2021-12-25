package com.hoc.flowmvi

import androidx.lifecycle.SavedStateHandle
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import io.mockk.every
import io.mockk.mockkClass
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Rule
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication
import org.koin.test.AutoCloseKoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule

@ExperimentalStdlibApi
@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
class CheckModulesTest : AutoCloseKoinTest() {
  @get:Rule
  val mockProvider = MockProviderRule.create { clazz ->
    mockkClass(clazz).also { o ->
      if (clazz == SavedStateHandle::class) {
        every { (o as SavedStateHandle).get<Any?>(any()) } returns null
      }
    }
  }
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()

  @Test
  fun verifyKoinApp() {
    koinApplication {
      modules(allModules)

      // TODO(koin): https://github.com/InsertKoinIO/koin/issues/1188
      printLogger(Level.ERROR)

      checkModules {
        withInstance<SavedStateHandle>()
      }
    }
  }
}
