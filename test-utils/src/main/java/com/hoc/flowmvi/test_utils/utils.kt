package com.hoc.flowmvi.test_utils

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.identity

inline val <L, R> Either<L, R>.leftValueOrThrow: L
  get() = fold(::identity) { throw AssertionError("Expect a Left but got a $this") }

inline val <L, R> Either<L, R>.rightValueOrThrow: R
  get() = getOrElse { throw AssertionError("Expect a Right but got a $this") }
