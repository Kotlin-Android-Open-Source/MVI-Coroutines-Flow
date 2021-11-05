package com.flowmvi.mvi_testing

import androidx.annotation.CallSuper
import arrow.core.Either
import arrow.core.right
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewModel
import com.hoc.flowmvi.mvi_base.MviViewState
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import io.mockk.clearAllMocks
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()

  protected val testDispatcher get() = coroutineRule.testCoroutineDispatcher

  @CallSuper
  @BeforeTest
  open fun setup() {
  }

  @CallSuper
  @AfterTest
  open fun tearDown() {
    clearAllMocks()
  }

  protected fun test(
    vmProducer: () -> VM,
    intents: Flow<I>,
    expectedStates: List<Either<(S) -> Unit, S>>,
    expectedEvents: List<Either<(E) -> Unit, E>>,
    delayAfterDispatchingIntents: Duration = Duration.ZERO,
    logging: Boolean = true,
    intentsBeforeCollecting: Flow<I>? = null,
    otherAssertions: (suspend () -> Unit)? = null,
  ) = testDispatcher.runBlockingTest {
    val vm = vmProducer()
    intentsBeforeCollecting?.collect { vm.processIntent(it) }

    val states = mutableListOf<S>()
    val events = mutableListOf<E>()

    val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { vm.viewState.toList(states) }
    val eventJob = launch(start = CoroutineStart.UNDISPATCHED) { vm.singleEvent.toList(events) }

    intents.collect { vm.processIntent(it) }
    delay(delayAfterDispatchingIntents)

    if (logging) {
      println(states)
      println(events)
    }

    assertEquals(expectedStates.size, states.size, "States size")
    expectedStates.withIndex().zip(states).forEach { (indexedValue, state) ->
      val (index, exp) = indexedValue
      exp.fold(
        ifRight = {
          assertEquals(
            expected = it,
            actual = state,
            message = "[State index=$index]"
          )
        },
        ifLeft = { it(state) }
      )
    }

    assertEquals(expectedEvents.size, events.size, "Events size")
    expectedEvents.withIndex().zip(events).forEach { (indexedValue, event) ->
      val (index, exp) = indexedValue
      exp.fold(
        ifRight = {
          assertEquals(
            expected = it,
            actual = event,
            message = "[Event index=$index]"
          )
        },
        ifLeft = { it(event) }
      )
    }

    otherAssertions?.invoke()
    stateJob.cancel()
    eventJob.cancel()
  }
}

fun <T> Iterable<T>.mapRight(): List<Either<(T) -> Unit, T>> = map { it.right() }
