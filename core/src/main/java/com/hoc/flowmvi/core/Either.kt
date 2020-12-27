package com.hoc.flowmvi.core

sealed class Either<out L, out R> {
  data class Left<L>(val l: L) : Either<L, Nothing>()
  data class Right<R>(val r: R) : Either<Nothing, R>()

  fun rightOrNull(): R? = when (this) {
    is Left -> null
    is Right -> r
  }

  fun leftOrNull(): L? = when (this) {
    is Left -> l
    is Right -> null
  }
}
