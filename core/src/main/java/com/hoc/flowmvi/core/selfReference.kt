package com.hoc.flowmvi.core

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Generic inline classes is an Experimental feature.
// It may be dropped or changed at any time.
// Opt-in is required with the -language-version 1.8 compiler option.
// See https://kotlinlang.org/docs/inline-classes.html for more information.
@JvmInline
value class SelfReference<T>(val value: T) : ReadOnlyProperty<Any?, T> {
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

/**
 * A delegate that allows to reference the object itself.
 * This is useful to avoid initialization order issues.
 *
 * This is a alternative way to:
 *  - `lateinit var` (mutable variables can be modified by mistake).
 *  - [lazy] (lazy evaluation is unnecessary in this case).
 *
 * NOTE: Do **NOT** access the value of the return delegate synchronously inside [initializer].
 * Eg: `val x: Int by selfReferenced { x + 1 }` is a wrong usage, it will cause an exception.
 *
 * ### Example
 * Below is an example of how to use it:
 *
 * ```kotlin
 * import kotlinx.coroutines.flow.*
 * import kotlinx.coroutines.*
 *
 * class Demo {
 *   private val trigger = MutableSharedFlow<Unit>()
 *   private val scope = CoroutineScope(Dispatchers.Default)
 *
 *   val intStateFlow: StateFlow<Int?> by selfReferenced {
 *     merge(
 *       flow {
 *        var c = 0
 *        while (true) { emit(c++); delay(300) }
 *       },
 *       trigger.mapNotNull {
 *         println("access to $intStateFlow")
 *         intStateFlow.value?.minus(1)
 *       }
 *     )
 *     .stateIn(scope, SharingStarted.Eagerly, null)
 *   }
 *
 *   fun trigger() = scope.launch { trigger.emit(Unit) }
 * }
 *
 * fun main(): Unit = runBlocking {
 *   val demo = Demo()
 *   val job = demo.intStateFlow
 *      .onEach(::println)
 *      .launchIn(this)
 *
 *   delay(1_000)
 *   demo.trigger()
 *
 *   delay(500)
 *   demo.trigger()
 *
 *   delay(500)
 *   job.cancel()
 *
 *   // null
 *   // 0
 *   // 1
 *   // 2
 *   // 3
 *   // access to kotlinx.coroutines.flow.ReadonlyStateFlow@2cfeac11
 *   // 2
 *   // 4
 *   // access to kotlinx.coroutines.flow.ReadonlyStateFlow@2cfeac11
 *   // 3
 *   // 5
 *   // 6
 * }
 * ```
 */
fun <T> selfReferenced(initializer: () -> T) = SelfReference(initializer())
