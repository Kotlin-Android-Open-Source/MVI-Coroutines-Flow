package com.hoc.flowmvi.mvi_testing

import androidx.annotation.CallSuper
import arrow.core.Either
import arrow.core.right
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewModel
import com.hoc.flowmvi.mvi_base.MviViewState
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import io.mockk.MockKAdditionalAnswerScope
import io.mockk.MockKStubScope
import io.mockk.clearAllMocks
import java.util.LinkedList
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule

fun <T> Iterable<T>.mapRight(): List<Either<(T) -> Unit, T>> = map { it.right() }

/**
 * Workaround for [Kotlin/kotlinx.coroutines/issues/3120](https://github.com/Kotlin/kotlinx.coroutines/issues/3120).
 * TODO(coroutines): https://github.com/Kotlin/kotlinx.coroutines/issues/3120
 */
fun <T> Flow<T>.delayEach() = onEach { delay(1) }

/**
 * Workaround for [Kotlin/kotlinx.coroutines/issues/3120](https://github.com/Kotlin/kotlinx.coroutines/issues/3120).
 * TODO(coroutines): https://github.com/Kotlin/kotlinx.coroutines/issues/3120
 */
infix fun <T, B> MockKStubScope<T, B>.returnsWithDelay(returnValue: T): MockKAdditionalAnswerScope<T, B> =
  coAnswers {
    delay(1)
    returnValue
  }

/**
 * Workaround for [Kotlin/kotlinx.coroutines/issues/3120](https://github.com/Kotlin/kotlinx.coroutines/issues/3120).
 * TODO(coroutines): https://github.com/Kotlin/kotlinx.coroutines/issues/3120
 */
infix fun <T, B> MockKStubScope<T, B>.returnsManyWithDelay(values: List<T>) {
  var count = 0
  coAnswers {
    delay(1)
    values[count++]
  }
}

@ExperimentalTime
@ExperimentalCoroutinesApi
abstract class BaseMviViewModelTest<
  I : MviIntent,
  S : MviViewState,
  E : MviSingleEvent,
  VM : MviViewModel<I, S, E>,
  > {
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule(
    testDispatcher = UnconfinedTestDispatcher(
      name = "${this::class.java.simpleName}-UnconfinedTestDispatcher",
    )
  )

  @CallSuper
  @BeforeTest
  open fun setup() {
  }

  @CallSuper
  @AfterTest
  open fun tearDown() {
    clearAllMocks()
  }

  protected fun runVMTest(
    vmProducer: () -> VM,
    intents: Flow<I>,
    expectedStates: List<Either<(S) -> Unit, S>>,
    expectedEvents: List<Either<(E) -> Unit, E>>,
    loggingEnabled: Boolean = BuildConfig.ENABLE_LOG_TEST,
    preProcessingIntents: Flow<I>? = null,
    otherAssertions: (suspend () -> Unit)? = null,
  ) = runTest(coroutineRule.testDispatcher) {
    val vm = vmProducer()
    preProcessingIntents?.let { prepare(vm, it, loggingEnabled) }

    // ------------------------------------- RUN -------------------------------------

    val (states, events, stateJob, eventJob) = run(
      vm = vm,
      intents = intents,
      expectedStates = expectedStates,
      expectedEvents = expectedEvents,
      loggingEnabled = loggingEnabled,
    )

    // ------------------------------------- DONE -------------------------------------

    advanceUntilIdle()
    runCurrent()

    stateJob.cancelAndJoin()
    eventJob.cancelAndJoin()

    logIfEnabled(loggingEnabled) { DIVIDER }
    logIfEnabled(loggingEnabled) { "[DONE] states=${states.joinToStringWithIndex()}" }
    logIfEnabled(loggingEnabled) { "[DONE] events=${events.joinToStringWithIndex()}" }

    // ------------------------------------- ASSERTIONS -------------------------------------

    assertEquals(expectedStates.size, states.size, "States size")
    assertEquals(expectedEvents.size, events.size, "Events size")
    doAssertions(expectedStates, states, expectedEvents, events)

    otherAssertions?.invoke()
  }

  private suspend fun TestScope.run(
    vm: VM,
    intents: Flow<I>,
    expectedStates: List<Either<(S) -> Unit, S>>,
    expectedEvents: List<Either<(E) -> Unit, E>>,
    loggingEnabled: Boolean,
  ): RunResult<S, E> {
    logIfEnabled(loggingEnabled) { "[START] $vm" }

    val states = LinkedList<S>()
    val events = LinkedList<E>()
    var stateIndex = 0
    var eventIndex = 0

    val stateJob = vm.viewState
      .onEach { state ->
        logIfEnabled(loggingEnabled) { "[STATE] <- $state" }

        states += state
        expectedStates[stateIndex].fold(
          ifRight = {
            assertEquals(
              expected = it,
              actual = state,
              message = "[State index=$stateIndex]"
            )
          },
          ifLeft = { it(state) }
        )
        ++stateIndex
      }
      .launchIn(this)

    val eventJob = vm.singleEvent
      .onEach { event ->
        logIfEnabled(loggingEnabled) { "[EVENT] <- $event" }

        events += event
        expectedEvents[eventIndex].fold(
          ifRight = {
            assertEquals(
              expected = it,
              actual = event,
              message = "[Event index=$eventIndex]"
            )
          },
          ifLeft = { it(event) }
        )
        ++eventIndex
      }
      .launchIn(this)

    intents.collect {
      logIfEnabled(loggingEnabled) { "[DISPATCH] Dispatch $it -> $vm" }
      vm.processIntent(it)
    }

    return RunResult(
      states = states,
      events = events,
      stateJob = stateJob,
      eventJob = eventJob,
    )
  }

  private suspend fun TestScope.prepare(
    vm: VM,
    intents: Flow<I>,
    loggingEnabled: Boolean,
  ) {
    val job1 = vm.viewState.launchIn(this)
    val job2 = vm.singleEvent.launchIn(this) // ignore events

    intents
      .onCompletion {
        advanceUntilIdle()
        runCurrent()

        job1.cancelAndJoin()
        job2.cancelAndJoin()
        logIfEnabled(loggingEnabled) { DIVIDER }
      }
      .collect {
        vm.processIntent(it)
        logIfEnabled(loggingEnabled) { "[BEFORE] Dispatch $it -> $vm" }
      }
  }
}

private data class RunResult<S, E>(
  val states: List<S>,
  val events: List<E>,
  val stateJob: Job,
  val eventJob: Job,
)

private val DIVIDER = "-".repeat(32)

private fun logIfEnabled(logging: Boolean, s: () -> String) = if (logging) println(s()) else Unit

private fun <S, E> doAssertions(
  expectedStates: List<Either<(S) -> Unit, S>>,
  states: List<S>,
  expectedEvents: List<Either<(E) -> Unit, E>>,
  events: List<E>,
) {
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
}

private fun <T> List<T>.joinToStringWithIndex(): String {
  return if (isEmpty()) "[]" else withIndex().joinToString(
    separator = ",\n",
    prefix = "[\n",
    postfix = "\n]",
  ) { (i, v) ->
    "   [$i]: $v"
  }
}
