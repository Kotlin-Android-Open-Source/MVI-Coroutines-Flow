package com.hoc.flowmvi.mvi_base

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Object that will subscribes to a MviView's [MviIntent]s,
 * process it and emit a [MviViewState] back.
 *
 * @param I Top class of the [MviIntent] that the [MviViewModel] will be subscribing to.
 * @param S Top class of the [MviViewState] the [MviViewModel] will be emitting.
 * @param E Top class of the [MviSingleEvent] that the [MviViewModel] will be emitting.
 */
interface MviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> {
  val viewState: StateFlow<S>

  val singleEvent: Flow<E>

  suspend fun processIntent(intent: I)
}

abstract class BaseMviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> :
  MviViewModel<I, S, E>, ViewModel() {
  private val tag by lazy(LazyThreadSafetyMode.PUBLICATION) { this::class.java.simpleName.take(23) }

  private val eventChannel = Channel<E>(Channel.UNLIMITED)
  private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = SubscriberBufferSize)

  override val singleEvent: Flow<E> get() = eventChannel.receiveAsFlow()
  override suspend fun processIntent(intent: I) = intentMutableFlow.emit(intent)

  protected suspend fun sendEvent(event: E) = eventChannel.send(event)
  protected val intentFlow: SharedFlow<I> get() = intentMutableFlow

  protected fun <T : I> Flow<T>.log(subject: String) = onEach { Log.d(tag, ">>> $subject: $it") }

  private companion object {
    /**
     * The buffer size that will be allocated by [kotlinx.coroutines.flow.MutableSharedFlow].
     * If it falls behind by more than 64 state updates, it will start suspending.
     * Slow consumers should consider using `stateFlow.buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)`.
     *
     * The internally allocated buffer is replay + extraBufferCapacity but always allocates 2^n space.
     * We use replay=0 so buffer = 64.
     */
    private const val SubscriberBufferSize = 64
  }
}
