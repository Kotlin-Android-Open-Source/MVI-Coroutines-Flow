package com.hoc.flowmvi.mvi_base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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
