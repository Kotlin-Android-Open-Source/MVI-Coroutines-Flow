package com.hoc.flowmvi

import android.view.View
import androidx.annotation.CheckResult
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalCoroutinesApi
@CheckResult
fun SwipeRefreshLayout.refreshes(): Flow<Unit> {
  return callbackFlow {
    setOnRefreshListener { offer(Unit) }
    awaitClose { setOnRefreshListener(null) }
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun View.clicks(): Flow<View> {
  return callbackFlow {
    setOnClickListener { offer(it) }
    awaitClose { setOnClickListener(null) }
  }
}

@FlowPreview
fun <T> merge(vararg flows: Flow<T>): Flow<T> = flows.asFlow().flattenMerge(flows.size)


@ExperimentalCoroutinesApi
fun <T, R> Flow<T>.flatMapFirst(transform: suspend (value: T) -> Flow<R>): Flow<R> =
  map(transform).flattenFirst()

@ExperimentalCoroutinesApi
fun <T> Flow<Flow<T>>.flattenFirst(): Flow<T> {
  return flow {
    val busy = AtomicBoolean(false)
    collect { inner ->
      if (busy.compareAndSet(false, true)) {
        emitAll(inner)
        busy.set(false)
      }
    }
  }
}
