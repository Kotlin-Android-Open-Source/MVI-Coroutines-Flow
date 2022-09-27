package com.hoc.flowmvi.test_utils

import arrow.core.Either
import arrow.core.Validated
import arrow.core.continuations.EffectScope
import arrow.core.getOrHandle
import arrow.core.identity
import arrow.core.valueOr
import com.hoc.flowmvi.core.unit
import io.mockk.MockKMatcherScope
import io.mockk.MockKStubScope

inline val <E, A> Validated<E, A>.valueOrThrow: A
  get() = valueOr(this::throws)

inline val <E, A> Validated<E, A>.invalidValueOrThrow: E
  get() = fold(::identity, this::throws)

inline val <L, R> Either<L, R>.leftOrThrow: L
  get() = fold(::identity, this::throws)

inline val <L, R> Either<L, R>.getOrThrow: R
  get() = getOrHandle(this::throws)

@PublishedApi
internal fun <E> Any.throws(it: E): Nothing =
  if (it is Throwable) throw it
  else error("$this - $it - Should not reach here!")

context(MockKMatcherScope)
inline fun <reified R, RR> withAnyEffectScope(block: EffectScope<R>.() -> RR): RR =
  with(any<EffectScope<R>>()) {
    block()
  }

inline infix fun <reified R> MockKStubScope<*, *>.thenShift(r: R): Unit =
  coAnswers { arg<EffectScope<R>>(0).shift(r) }.unit
