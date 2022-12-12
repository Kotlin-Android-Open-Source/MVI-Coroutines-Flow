package com.hoc.flowmvi.core

/**
 * `NonEmptySet` is a data type used to model sets that guarantee to have at least one value.
 */
class NonEmptySet<out T>
@Throws(IllegalArgumentException::class)
private constructor(val set: Set<T>) : AbstractSet<T>() {
  init {
    require(set.isNotEmpty()) { "Set must not be empty" }
    require(set !is NonEmptySet<T>) { "Set must not be NonEmptySet" }
  }

  override val size: Int get() = set.size
  override fun iterator(): Iterator<T> = set.iterator()
  override fun isEmpty(): Boolean = false

  operator fun plus(l: NonEmptySet<@UnsafeVariance T>): NonEmptySet<T> =
    NonEmptySet(set + l.set)

  @Suppress("RedundantOverride")
  override fun equals(other: Any?): Boolean = super.equals(other)

  @Suppress("RedundantOverride")
  override fun hashCode(): Int = super.hashCode()

  override fun toString(): String =
    "NonEmptySet(${set.joinToString()})"

  companion object {
    /**
     * Creates a [NonEmptySet] from the given [Collection].
     * @return null if [this] is empty.
     */
    @JvmStatic
    fun <T> Collection<T>.toNonEmptySetOrNull(): NonEmptySet<T>? =
      if (isEmpty()) null else NonEmptySet(toSet())

    /**
     * Creates a [NonEmptySet] from the given [Set].
     * @return null if [this] is empty.
     */
    @JvmStatic
    fun <T> Set<T>.toNonEmptySetOrNull(): NonEmptySet<T>? = (this as? NonEmptySet<T>)
      ?: if (isEmpty()) null else NonEmptySet(this)

    /**
     * Creates a [NonEmptySet] from the given values.
     */
    @JvmStatic
    fun <T> of(element: T, vararg elements: T): NonEmptySet<T> = NonEmptySet(
      buildSet(capacity = 1 + elements.size) {
        add(element)
        addAll(elements)
      }
    )

    /**
     * Creates a [NonEmptySet] that contains only the specified [element].
     */
    @JvmStatic
    fun <T> of(element: T): NonEmptySet<T> = NonEmptySet(setOf(element))
  }
}
