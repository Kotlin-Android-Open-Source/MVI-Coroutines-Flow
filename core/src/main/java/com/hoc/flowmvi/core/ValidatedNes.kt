package com.hoc.flowmvi.core

import arrow.core.Validated
import arrow.typeclasses.Semigroup

typealias ValidatedNes<E, A> = Validated<NonEmptySet<E>, A>

@Suppress("NOTHING_TO_INLINE")
inline fun <A> A.validNes(): ValidatedNes<Nothing, A> =
  Validated.Valid(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <E> E.invalidNes(): ValidatedNes<E, Nothing> =
  Validated.Invalid(NonEmptySet.of(this))

object NonEmptySetSemigroup : Semigroup<NonEmptySet<Any?>> {
  override fun NonEmptySet<Any?>.combine(b: NonEmptySet<Any?>): NonEmptySet<Any?> = this + b
}

@Suppress("UNCHECKED_CAST")
fun <T> Semigroup.Companion.nonEmptySet(): Semigroup<NonEmptySet<T>> =
  NonEmptySetSemigroup as Semigroup<NonEmptySet<T>>
