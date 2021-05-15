package com.hoc.flowmvi.core

import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.appcompat.widget.SearchView
import androidx.core.widget.doOnTextChanged
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.EmptyCoroutineContext

internal fun checkMainThread() {
  check(Looper.myLooper() == Looper.getMainLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.firstChange(): Flow<Unit> {
  return callbackFlow {
    checkMainThread()

    val listener = doOnTextChanged { _, _, _, _ -> trySend(Unit) }
    awaitClose {
      Dispatchers.Main.dispatch(EmptyCoroutineContext) {
        removeTextChangedListener(listener)
        Log.d("###", "removeTextChangedListener $listener ${this@firstChange}")
      }
    }
  }.take(1)
}

@ExperimentalCoroutinesApi
@CheckResult
fun SwipeRefreshLayout.refreshes(): Flow<Unit> {
  return callbackFlow {
    checkMainThread()

    setOnRefreshListener { trySend(Unit) }
    awaitClose { setOnRefreshListener(null) }
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun View.clicks(): Flow<View> {
  return callbackFlow {
    checkMainThread()

    setOnClickListener { trySend(it) }
    awaitClose { setOnClickListener(null) }
  }
}

data class SearchViewQueryTextEvent(
  val view: SearchView,
  val query: CharSequence,
  val isSubmitted: Boolean,
)

@ExperimentalCoroutinesApi
@CheckResult
fun SearchView.queryTextEvents(): Flow<SearchViewQueryTextEvent> {
  return callbackFlow {
    checkMainThread()

    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        return trySend(
          SearchViewQueryTextEvent(
            view = this@queryTextEvents,
            query = query,
            isSubmitted = true,
          )
        ).isSuccess
      }

      override fun onQueryTextChange(newText: String): Boolean {
        return trySend(
          SearchViewQueryTextEvent(
            view = this@queryTextEvents,
            query = newText,
            isSubmitted = false,
          )
        ).isSuccess
      }
    })

    awaitClose { setOnQueryTextListener(null) }
  }.onStart {
    emit(
      SearchViewQueryTextEvent(
        view = this@queryTextEvents,
        query = query,
        isSubmitted = false,
      )
    )
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
  return callbackFlow {
    checkMainThread()

    val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
  }.onStart { emit(text) }
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

fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

@ExperimentalCoroutinesApi
suspend fun main() {
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
