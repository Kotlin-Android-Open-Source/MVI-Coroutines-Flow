package com.hoc.flowmvi.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@ExperimentalCoroutinesApi
fun <T, R> Flow<T>.takeUntil(notifier: Flow<R>): Flow<T> = channelFlow {
  val outerScope = this

  launch {
    try {
      notifier.take(1).collect()
      close()
    } catch (e: CancellationException) {
      outerScope.cancel(e) // cancel outer scope on cancellation exception, too
    }
  }
  launch {
    try {
      collect { send(it) }
      close()
    } catch (e: CancellationException) {
      outerScope.cancel(e) // cancel outer scope on cancellation exception, too
    }
  }
}

@ExperimentalCoroutinesApi
fun <T, R> Flow<T>.flatMapFirst(transform: suspend (value: T) -> Flow<R>): Flow<R> =
  map(transform).flattenFirst()

@ExperimentalCoroutinesApi
fun <T> Flow<Flow<T>>.flattenFirst(): Flow<T> = channelFlow {
  val outerScope = this
  val busy = AtomicBoolean(false)
  collect { inner ->
    if (busy.compareAndSet(false, true)) {
      launch {
        try {
          inner.collect { outerScope.send(it) }
          busy.set(false)
        } catch (e: CancellationException) {
          // cancel outer scope on cancellation exception, too
          outerScope.cancel(e)
        }
      }
    }
  }
}

private object UNINITIALIZED

fun <A, B, R> Flow<A>.withLatestFrom(other: Flow<B>, transform: suspend (A, B) -> R): Flow<R> {
  return flow {
    coroutineScope {
      val latestB = AtomicReference<Any>(UNINITIALIZED)
      val outerScope = this

      launch {
        try {
          other.collect { latestB.set(it) }
        } catch (e: CancellationException) {
          outerScope.cancel(e) // cancel outer scope on cancellation exception, too
        }
      }

      collect { a ->
        val b = latestB.get()
        if (b != UNINITIALIZED) {
          @Suppress("UNCHECKED_CAST")
          emit(transform(a, b as B))
        }
      }
    }
  }
}

@ExperimentalCoroutinesApi
suspend fun main() {
  (1..2).asFlow()
    .onEach { delay(50) }
    .takeUntil(flow { delay(100000); emit(Unit) })
    .collect { println(">>>>> $it") }

  println("Done")

  (1..2000).asFlow()
    .onEach { delay(50) }
    .flatMapFirst { v ->
      flow {
        delay(500)
        emit(v)
      }
    }
    .onEach { println("[*] $it") }
    .catch { println("Error $it") }
    .collect()
}
