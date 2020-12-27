package com.hoc.flowmvi.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

fun <T> Flow<T>.launchWhenStartedUntilStopped(owner: LifecycleOwner) {
  if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
    // ignore
    return
  }
  owner.lifecycle.addObserver(LifecycleBoundObserver(this))
}

private class LifecycleBoundObserver(private val flow: Flow<*>) : DefaultLifecycleObserver {
  private var job: Job? = null

  override fun onStart(owner: LifecycleOwner) {
    job = flow.launchIn(owner.lifecycleScope)
  }

  override fun onStop(owner: LifecycleOwner) {
    cancelJob()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    owner.lifecycle.removeObserver(this)
    cancelJob()
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun cancelJob() {
    job?.cancel()
    job = null
  }
}
