package com.hoc.flowmvi.core

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.addRepeatingJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <T> Flow<T>.collectIn(
  owner: LifecycleOwner,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  coroutineContext: CoroutineContext = EmptyCoroutineContext,
  crossinline action: suspend (value: T) -> Unit,
): Job = owner.addRepeatingJob(state = minActiveState, coroutineContext = coroutineContext) {
  Log.d("collectIn", "Start collecting...")
  collect { action(it) }
}

@Suppress("unused")
inline fun <T> Flow<T>.collectIn(
  fragment: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  coroutineContext: CoroutineContext = EmptyCoroutineContext,
  crossinline action: suspend (value: T) -> Unit,
): Job = collectIn(
  fragment.viewLifecycleOwner,
  minActiveState = minActiveState,
  coroutineContext = coroutineContext,
  action = action,
)
