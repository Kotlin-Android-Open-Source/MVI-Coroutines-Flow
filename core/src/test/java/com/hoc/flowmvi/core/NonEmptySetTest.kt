package com.hoc.flowmvi.core

import com.hoc.flowmvi.core.NonEmptySet.Companion.toNonEmptySetOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class NonEmptySetTest {
  @Test
  fun `test List#toNonEmptySetOrNull returns null when input is empty`() {
    assertNull(emptyList<Int>().toNonEmptySetOrNull())
  }

  @Test
  fun `test List#toNonEmptySetOrNull returns NonEmptySet when input is not empty`() {
    assertEquals(
      setOf(1),
      assertNotNull(
        listOf(1).toNonEmptySetOrNull()
      ),
    )

    assertEquals(
      setOf(1, 2),
      assertNotNull(
        listOf(1, 2).toNonEmptySetOrNull(),
      ),
    )

    assertEquals(
      setOf(1),
      assertNotNull(
        listOf(1, 1).toNonEmptySetOrNull()
      ),
    )
  }

  @Test
  fun `test Set#toNonEmptySetOrNull returns null when input is empty`() {
    assertNull(emptySet<Int>().toNonEmptySetOrNull())
  }

  @Test
  fun `test Set#toNonEmptySetOrNull returns NonEmptySet when input is not empty`() {
    assertEquals(
      setOf(1),
      assertNotNull(
        setOf(1).toNonEmptySetOrNull()
      )
    )

    assertEquals(
      setOf(1, 2),
      assertNotNull(
        setOf(1, 2).toNonEmptySetOrNull()
      )
    )

    assertEquals(
      setOf(1),
      assertNotNull(
        setOf(1, 1).toNonEmptySetOrNull()
      )
    )
  }

  @Test
  fun `test Set#toNonEmptySetOrNull returns itself when the input is NonEmptySet`() {
    val input = NonEmptySet.of(1)

    assertSame(
      input,
      input.toNonEmptySetOrNull()
    )
  }

  @Test
  fun `test NonEmptySet#of`() {
    assertEquals(
      setOf(1),
      NonEmptySet.of(1),
    )

    assertEquals(
      setOf(1, 2),
      NonEmptySet.of(1, 2),
    )

    assertEquals(
      setOf(1),
      NonEmptySet.of(1, 1),
    )
  }

  @Test
  fun `test NonEmptySet#equals`() {
    assertEquals(
      NonEmptySet.of(1),
      NonEmptySet.of(1),
    )
    assertEquals(
      NonEmptySet.of(1, 2),
      NonEmptySet.of(1, 2),
    )
    assertEquals(
      NonEmptySet.of(1, 1),
      NonEmptySet.of(1, 1),
    )

    assertEquals(
      listOf(1, 2).toNonEmptySetOrNull(),
      listOf(1, 2).toNonEmptySetOrNull(),
    )

    assertEquals(
      hashSetOf(1, 2).toNonEmptySetOrNull(),
      linkedSetOf(1, 2).toNonEmptySetOrNull(),
    )
    assertEquals(
      setOf(1, 2).toNonEmptySetOrNull(),
      setOf(1, 2).toNonEmptySetOrNull(),
    )
  }

  @Test
  fun `test NonEmptySet#hashCode`() {
    assertEquals(
      NonEmptySet.of(1).hashCode(),
      NonEmptySet.of(1).hashCode(),
    )
    assertEquals(
      NonEmptySet.of(1, 2).hashCode(),
      NonEmptySet.of(1, 2).hashCode(),
    )
    assertEquals(
      NonEmptySet.of(1, 1).hashCode(),
      NonEmptySet.of(1, 1).hashCode(),
    )

    assertEquals(
      listOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
      listOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
    )

    assertEquals(
      hashSetOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
      linkedSetOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
    )
    assertEquals(
      setOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
      setOf(1, 2).toNonEmptySetOrNull()!!.hashCode(),
    )
  }
}
