package com.flowmvi.mvi_testing

import androidx.annotation.CallSuper
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewModel
import com.hoc.flowmvi.mvi_base.MviViewState
import io.mockk.clearAllMocks
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
abstract class BaseMviViewModelTest<
  I : MviIntent,
  S : MviViewState,
  E : MviSingleEvent,
  VM : MviViewModel<I, S, E>,
  > {
  private val testDispatcher = TestCoroutineDispatcher()

  @CallSuper
  @BeforeTest
  open fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  protected fun test(
    vmProducer: () -> VM,
    intents: Flow<I>,
    expectedStates: List<S>,
    expectedEvents: List<E>,
    delayAfterDispatchingIntents: Duration = Duration.milliseconds(50),
    otherAssertions: (suspend () -> Unit)? = null,
  ) = testDispatcher.runBlockingTest {
    val vm = vmProducer()
    val states = mutableListOf<S>()
    val events = mutableListOf<E>()

    val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { vm.viewState.toList(states) }
    val eventJob = launch(start = CoroutineStart.UNDISPATCHED) { vm.singleEvent.toList(events) }

    intents.collect { vm.processIntent(it) }
    delay(delayAfterDispatchingIntents)

    assertEquals(expectedStates.size, states.size)
    assertContentEquals(
      expectedStates,
      states,
    )

    assertEquals(expectedEvents.size, events.size)
    assertContentEquals(
      expectedEvents,
      events,
    )

    otherAssertions?.invoke()
    stateJob.cancel()
    eventJob.cancel()
  }

  @CallSuper
  @AfterTest
  open fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
    clearAllMocks()
  }
}
