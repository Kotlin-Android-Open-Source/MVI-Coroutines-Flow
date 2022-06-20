package com.hoc.flowmvi.mvi_base

import androidx.annotation.CheckResult
import kotlinx.coroutines.flow.Flow

/**
 * Object representing a UI that will
 * a) emit its intents to a view model,
 * b) subscribes to a view model for rendering its UI.
 * c) subscribes to a view model for handling single UI event.
 *
 * @param I Top class of the [MviIntent] that the [MviView] will be emitting.
 * @param S Top class of the [MviViewState] the [MviView] will be subscribing to.
 * @param E Top class of the [MviSingleEvent] the [MviView] will be subscribing to.
 */
interface MviView<I : MviIntent,
  S : MviViewState,
  E : MviSingleEvent,> {
  /**
   * Entry point for the [MviView] to render itself based on a [MviViewState].
   */
  fun render(viewState: S)

  /**
   * Entry point for the [MviView] to handle single event.
   */
  fun handleSingleEvent(event: E)

  /**
   * Unique [Flow] used by the [MviViewModel] to listen to the [MviView].
   * All the [MviView]'s [MviIntent]s must go through this [Flow].
   */
  @CheckResult
  fun viewIntents(): Flow<I>
}
