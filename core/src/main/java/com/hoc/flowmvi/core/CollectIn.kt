package com.hoc.flowmvi.core

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.collectIn(
  owner: LifecycleOwner,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  crossinline action: suspend (value: T) -> Unit,
): Job = owner.lifecycleScope.launch {
  owner.lifecycle.repeatOnLifecycle(state = minActiveState) {
    Log.d("collectIn", "Start collecting $owner $minActiveState...")
    collect { action(it) }
  }
}

@Suppress("unused")
inline fun <T> Flow<T>.collectIn(
  fragment: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  crossinline action: suspend (value: T) -> Unit,
): Job = fragment.lifecycleScope.launch {
  fragment.viewLifecycleOwner.repeatOnLifecycle(state = minActiveState) {
    Log.d("collectIn", "Start collecting $fragment $minActiveState...")
    collect { action(it) }
  }
}
