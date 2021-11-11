package com.hoc.flowmvi.test_utils

import arrow.core.Validated
import arrow.core.valueOr

inline val <E, A> Validated<E, A>.valueOrThrow: A
  get() {
    return valueOr {
      if (it is Throwable) throw it
      else error("$this - $it - Should not reach here!")
    }
  }
