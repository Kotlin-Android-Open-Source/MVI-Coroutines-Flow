package com.hoc.flowmvi.mvi_base

import android.os.Build
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
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
  protected val logTag by lazy(LazyThreadSafetyMode.PUBLICATION) {
    this::class.java.simpleName.let { tag: String ->
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

  final override val singleEvent: Flow<E> get() = eventChannel.receiveAsFlow()
  final override suspend fun processIntent(intent: I) = intentMutableFlow.emit(intent)

  @CallSuper
  override fun onCleared() {
    super.onCleared()
    eventChannel.close()
  }

  // Send event and access intent flow.

  protected suspend fun sendEvent(event: E) = eventChannel.send(event)
  protected val intentFlow: SharedFlow<I> get() = intentMutableFlow

  // Extensions on Flow using viewModelScope.

  protected fun <T> Flow<T>.log(subject: String): Flow<T> =
    onEach { Timber.tag(logTag).d(">>> $subject: $it") }

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
