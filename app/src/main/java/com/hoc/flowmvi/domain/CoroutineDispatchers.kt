package com.hoc.flowmvi.domain

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
}