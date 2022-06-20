package com.hoc.flowmvi.core_ui

import android.os.Looper
import android.view.View
import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.appcompat.widget.SearchView
import androidx.core.widget.doOnTextChanged
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hoc081098.flowext.startWith
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take
import timber.log.Timber

internal fun checkMainThread() {
  check(Looper.myLooper() == Looper.getMainLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
  }
}

@CheckResult
fun EditText.firstChange(): Flow<Unit> {
  return callbackFlow {
    checkMainThread()

    val listener = doOnTextChanged { _, _, _, _ -> trySend(Unit) }
    awaitClose {
      Dispatchers.Main.dispatch(EmptyCoroutineContext) {
        removeTextChangedListener(listener)
        Timber.d("removeTextChangedListener $listener $this")
      }
    }
  }.take(1)
}

@CheckResult
fun SwipeRefreshLayout.refreshes(): Flow<Unit> {
  return callbackFlow {
    checkMainThread()

    setOnRefreshListener { trySend(Unit) }
    awaitClose { setOnRefreshListener(null) }
  }
}

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

@CheckResult
fun SearchView.queryTextEvents(): Flow<SearchViewQueryTextEvent> {
  return callbackFlow {
    checkMainThread()

    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        trySend(
          SearchViewQueryTextEvent(
            view = this@queryTextEvents,
            query = query,
            isSubmitted = true,
          )
        )
        return false
      }

      override fun onQueryTextChange(newText: String): Boolean {
        trySend(
          SearchViewQueryTextEvent(
            view = this@queryTextEvents,
            query = newText,
            isSubmitted = false,
          )
        )
        return true
      }
    })

    awaitClose { setOnQueryTextListener(null) }
  }.startWith {
    SearchViewQueryTextEvent(
      view = this@queryTextEvents,
      query = query,
      isSubmitted = false,
    )
  }
}

@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
  return callbackFlow {
    checkMainThread()

    val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
    awaitClose { removeTextChangedListener(listener) }
  }.startWith { text }
}
