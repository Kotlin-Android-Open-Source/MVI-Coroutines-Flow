package com.hoc.flowmvi

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Rule
import org.koin.test.check.checkKoinModules
import org.koin.test.mock.MockProviderRule
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalStdlibApi
@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
class ExampleUnitTest {
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
    checkKoinModules(allModules) {
      withInstance<SavedStateHandle>()
    }
  }
}
