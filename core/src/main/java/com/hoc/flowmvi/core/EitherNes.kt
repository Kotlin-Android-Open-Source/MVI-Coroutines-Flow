package com.hoc.flowmvi.core

import arrow.core.Either
import arrow.core.NonEmptySet
import arrow.core.left
import arrow.core.nonEmptySetOf
import arrow.core.right

/**
 * A typealias for [Either] with [NonEmptySet] as the left side.
 */
typealias EitherNes<E, A> = Either<NonEmptySet<E>, A>

@Suppress("NOTHING_TO_INLINE")
inline fun <A> A.rightNes(): EitherNes<Nothing, A> = this.right()

@Suppress("NOTHING_TO_INLINE")
inline fun <E> E.leftNes(): EitherNes<E, Nothing> = nonEmptySetOf(this).left()

inline fun <E, A, B, C, Z> Either.Companion.zipOrAccumulateNonEmptySet(
  a: EitherNes<E, A>,
  b: EitherNes<E, B>,
  c: EitherNes<E, C>,
  transform: (A, B, C) -> Z,
): EitherNes<E, Z> =
  zipOrAccumulate(
    combine = { acc, value -> acc + value },
    a = a,
    b = b,
    c = c,
    transform = transform,
  )
