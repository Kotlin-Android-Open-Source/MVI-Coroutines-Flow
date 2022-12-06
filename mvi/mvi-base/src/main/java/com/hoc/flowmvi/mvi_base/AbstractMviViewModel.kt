package com.hoc.flowmvi.mvi_base

import android.os.Build
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

abstract class AbstractMviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> :
  MviViewModel<I, S, E>, ViewModel() {
  protected open val rawLogTag: String? = null

  protected val logTag by lazy(PUBLICATION) {
    (rawLogTag ?: this::class.java.simpleName).let { tag: String ->
      // Tag length limit was removed in API 26.
      if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
        tag
      } else {
        tag.take(MAX_TAG_LENGTH)
      }
    }
  }

  private val eventChannel = Channel<E>(Channel.UNLIMITED)
  private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = SubscriberBufferSize)

  final override val singleEvent: Flow<E> = eventChannel.receiveAsFlow()
  final override suspend fun processIntent(intent: I) = intentMutableFlow.emit(intent)

  @CallSuper
  override fun onCleared() {
    super.onCleared()
    eventChannel.close()
    Timber.tag(logTag).d("onCleared")
  }

  // Send event and access intent flow.

  /**
   * Must be called in [kotlinx.coroutines.Dispatchers.Main.immediate],
   * otherwise it will throw an exception.
   *
   * If you want to send an event from other [kotlinx.coroutines.CoroutineDispatcher],
   * use `withContext(Dispatchers.Main.immediate) { sendEvent(event) }`.
   */
  protected suspend fun sendEvent(event: E) {
    debugCheckImmediateMainDispatcher()

    eventChannel.trySend(event)
      .onFailure {
        Timber
          .tag(logTag)
          .e(it, "Failed to send event: $event")
      }
      .getOrThrow()
  }

  protected val intentFlow: SharedFlow<I> get() = intentMutableFlow

  // Extensions on Flow using viewModelScope.

  protected fun <T> Flow<T>.debugLog(subject: String): Flow<T> =
    if (BuildConfig.DEBUG) {
      onEach { Timber.tag(logTag).d(">>> $subject: $it") }
    } else {
      this
    }

  protected fun <T> Flow<T>.shareWhileSubscribed(): SharedFlow<T> =
    shareIn(viewModelScope, SharingStarted.WhileSubscribed())

  protected fun <T> Flow<T>.stateWithInitialNullWhileSubscribed(): StateFlow<T?> =
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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

    private const val MAX_TAG_LENGTH = 23
  }
}
