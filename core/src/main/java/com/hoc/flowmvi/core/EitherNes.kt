package com.hoc.flowmvi.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.core.NonEmptySet.Companion.toNonEmptySetOrNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A typealias for [Either] with [NonEmptySet] as the left side.
 */
typealias EitherNes<E, A> = Either<NonEmptySet<E>, A>

@Suppress("NOTHING_TO_INLINE")
inline fun <A> A.rightNes(): EitherNes<Nothing, A> = this.right()

@Suppress("NOTHING_TO_INLINE")
inline fun <E> E.leftNes(): EitherNes<E, Nothing> =
  NonEmptySet.of(this).left()

@OptIn(ExperimentalContracts::class)
inline fun <E, A, B, C, Z> Either.Companion.zipOrAccumulateNonEmptySet(
  a: EitherNes<E, A>,
  b: EitherNes<E, B>,
  c: EitherNes<E, C>,
  transform: (A, B, C) -> Z,
): EitherNes<E, Z> {
  contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }

  return if (
    a is Either.Right &&
    b is Either.Right &&
    c is Either.Right
  ) {
    Either.Right(
      transform(
        a.value,
        b.value,
        c.value,
      )
    )
  } else {
    Either.Left(
      buildSet(capacity = a.count + b.count + c.count) {
        if (a is Either.Left) this.addAll(a.value)
        if (b is Either.Left) this.addAll(b.value)
        if (c is Either.Left) this.addAll(c.value)
      }.toNonEmptySetOrNull()!!
    )
  }
}

@PublishedApi
internal inline val <L, R> Either<L, R>.count: Int get() = if (isRight()) 1 else 0
