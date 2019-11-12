package com.hoc.flowmvi

import android.view.View
import androidx.annotation.CheckResult
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flattenMerge

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