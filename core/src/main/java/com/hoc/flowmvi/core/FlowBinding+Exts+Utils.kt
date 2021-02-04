package com.hoc.flowmvi.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.SendChannel
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

fun <T> SendChannel<T>.safeOffer(element: T): Boolean {
  return runCatching { offer(element) }.getOrDefault(false)
}

@ExperimentalCoroutinesApi
fun EditText.firstChange(): Flow<Unit> {
  return callbackFlow {
    val listener = doOnTextChanged { _, _, _, _ -> safeOffer(Unit) }
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
    setOnRefreshListener { safeOffer(Unit) }
    awaitClose { setOnRefreshListener(null) }
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun View.clicks(): Flow<View> {
  return callbackFlow {
    setOnClickListener { safeOffer(it) }
    awaitClose { setOnClickListener(null) }
  }
}

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
  return callbackFlow<CharSequence?> {
    val listener = doOnTextChanged { text, _, _, _ -> safeOffer(text) }
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

class SwipeLeftToDeleteCallback(context: Context, private val onSwipedCallback: (Int) -> Unit) :
  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
  private val background: ColorDrawable = ColorDrawable(Color.parseColor("#f44336"))
  private val iconDelete =
    ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_white_24)!!

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ) = false

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    val position = viewHolder.bindingAdapterPosition
    if (position != RecyclerView.NO_POSITION) {
      onSwipedCallback(position)
    }
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    val itemView = viewHolder.itemView

    when {
      dX < 0 -> {
        val iconMargin = (itemView.height - iconDelete.intrinsicHeight) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + iconDelete.intrinsicHeight

        val iconRight = itemView.right - iconMargin
        val iconLeft = iconRight - iconDelete.intrinsicWidth

        iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        background.setBounds(
          itemView.right + dX.toInt() - 8,
          itemView.top,
          itemView.right,
          itemView.bottom
        )
      }
      else -> background.setBounds(0, 0, 0, 0)
    }
    background.draw(c)
    iconDelete.draw(c)
  }
}
