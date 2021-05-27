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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
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

fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
