package com.hoc.flowmvi

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Rule
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication
import org.koin.test.AutoCloseKoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule
import kotlin.test.Test
import kotlin.time.ExperimentalTime

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
