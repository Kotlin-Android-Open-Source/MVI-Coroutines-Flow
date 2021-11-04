@file:Suppress("unused", "ClassName", "SpellCheckingInspection")

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

const val ktlintVersion = "0.41.0"
const val kotlinVersion = "1.5.31"

object appConfig {
  const val applicationId = "com.hoc.flowmvi"

  const val compileSdkVersion = 31
  const val buildToolsVersion = "31.0.0"

  const val minSdkVersion = 21
  const val targetSdkVersion = 31

  private const val MAJOR = 2
  private const val MINOR = 0
  private const val PATCH = 0
  const val versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
  const val versionName = "$MAJOR.$MINOR.$PATCH"
}

object deps {
  object androidx {
    const val appCompat = "androidx.appcompat:appcompat:1.4.0-rc01"
    const val coreKtx = "androidx.core:core-ktx:1.7.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"
    const val material = "com.google.android.material:material:1.4.0"
  }

  object lifecycle {
    private const val version = "2.4.0"

    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version" // viewModelScope
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version" // lifecycleScope
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
  }

  object squareup {
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.12.0"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.7"
  }

  object coroutines {
    private const val version = "1.5.2"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
  }

  object koin {
    private const val version = "3.1.3"

    const val core = "io.insert-koin:koin-core:$version"
    const val android = "io.insert-koin:koin-android:$version"
    const val testJunit4 = "io.insert-koin:koin-test-junit4:$version"
  }

  const val coil = "io.coil-kt:coil:1.2.1"
  const val viewBindingDelegate = "com.github.hoc081098:ViewBindingDelegate:1.2.0"
  const val flowExt = "io.github.hoc081098:FlowExt:0.0.7-SNAPSHOT"
  const val timber = "com.jakewharton.timber:timber:5.0.1"

  object arrow {
    private const val version = "1.0.1"
    const val core = "io.arrow-kt:arrow-core:$version"
  }

  object test {
    const val junit = "junit:junit:4.13.2"
    const val androidxJunit = "androidx.test.ext:junit:1.1.2"
    const val androidXSspresso = "androidx.test.espresso:espresso-core:3.3.0"

    const val mockk = "io.mockk:mockk:1.12.0"
    const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
  }
}

private typealias PDsS = PluginDependenciesSpec
private typealias PDS = PluginDependencySpec

inline val PDsS.androidApplication: PDS get() = id("com.android.application")
inline val PDsS.androidLib: PDS get() = id("com.android.library")
inline val PDsS.kotlinAndroid: PDS get() = id("kotlin-android")
inline val PDsS.kotlin: PDS get() = id("kotlin")
inline val PDsS.kotlinKapt: PDS get() = id("kotlin-kapt")

inline val DependencyHandler.domain get() = project(":domain")
inline val DependencyHandler.core get() = project(":core")
inline val DependencyHandler.data get() = project(":data")
inline val DependencyHandler.featureMain get() = project(":feature-main")
inline val DependencyHandler.featureAdd get() = project(":feature-add")
inline val DependencyHandler.featureSearch get() = project(":feature-search")
inline val DependencyHandler.mviBase get() = project(":mvi-base")
inline val DependencyHandler.mviTesting get() = project(":mvi-testing")

fun DependencyHandler.addUnitTest(testImplementation: Boolean = true) {
  val configName = if (testImplementation) "testImplementation" else "implementation"

  add(configName, deps.test.junit)
  add(configName, deps.test.mockk)
  add(configName, deps.test.kotlinJUnit)
  add(configName, deps.coroutines.test)
}
