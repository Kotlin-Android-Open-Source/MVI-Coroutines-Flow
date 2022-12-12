package com.hoc.flowmvi

import androidx.lifecycle.SavedStateHandle
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    when (clazz) {
      SavedStateHandle::class -> {
        mockk<SavedStateHandle> {
          every { get<Any?>(any()) } returns null
          every { setSavedStateProvider(any(), any()) } just runs
        }
      }
      else -> error("Unknown class: $clazz")
    }
  }

  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()

  @Test
  fun verifyKoinApp() {
    koinApplication {
      modules(allModules)

      printLogger(Level.DEBUG)

      checkModules {
        withInstance<SavedStateHandle>()
      }
    }
  }
}
