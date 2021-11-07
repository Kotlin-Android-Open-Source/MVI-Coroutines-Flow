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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
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
    logging: Boolean = BuildConfig.ENABLE_LOG_TEST,
    intentsBeforeCollecting: Flow<I>? = null,
    otherAssertions: (suspend () -> Unit)? = null,
  ) = testDispatcher.runBlockingTest {
    fun logIfEnabled(s: () -> String) = if (logging) println(s()) else Unit

    val vm = vmProducer()
    intentsBeforeCollecting?.let { flow ->
      val job = vm.singleEvent.launchIn(this) // ignore events

      flow
        .onCompletion {
          job.cancel()
          logIfEnabled { "-".repeat(32) }
        }
        .collect {
          vm.processIntent(it)
          logIfEnabled { "[BEFORE] Dispatch $it -> $vm" }
        }
    }

    logIfEnabled { "[START] $vm" }

    val states = mutableListOf<S>()
    val events = mutableListOf<E>()

    val stateJob = launch(start = CoroutineStart.UNDISPATCHED) {
      vm.viewState.onEach { logIfEnabled { "[STATE] <- $it" } }.toList(states)
    }
    val eventJob = launch(start = CoroutineStart.UNDISPATCHED) { vm.singleEvent.toList(events) }

    intents.collect {
      logIfEnabled { "[DISPATCH] Dispatch $it -> $vm" }
      vm.processIntent(it)
    }
    delay(delayAfterDispatchingIntents)
    logIfEnabled { "-".repeat(32) }
    stateJob.cancel()
    eventJob.cancel()

    logIfEnabled { "[DONE] states=${states.joinToStringWithIndex()}" }
    logIfEnabled { "[DONE] events=${events.joinToStringWithIndex()}" }

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
  }
}

fun <T> Iterable<T>.mapRight(): List<Either<(T) -> Unit, T>> = map { it.right() }

private fun <T> List<T>.joinToStringWithIndex(): String {
  return withIndex().joinToString(
    separator = ",\n",
    prefix = "[\n",
    postfix = "]",
  ) { (i, v) ->
    "   [$i]: $v"
  }
}
