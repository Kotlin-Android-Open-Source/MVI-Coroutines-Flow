import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.EnumSet
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.kapt) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.cocoapods) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.jetbrains.compose) apply false
  alias(libs.plugins.android.app) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.ben.manes.versions) apply false
  alias(libs.plugins.gradle.spotless) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kotlinx.kover)
  alias(libs.plugins.poko) apply false
  alias(libs.plugins.ksp) apply false
}

subprojects {
  apply(plugin = rootProject.libs.plugins.ben.manes.versions.get().pluginId)

  fun isNonStable(version: String): Boolean {
    val stableKeyword =
      listOf("RELEASE", "FINAL", "GA")
        .any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
  }

  fun isStable(version: String) = !isNonStable(version)

  tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
      if (isStable(currentVersion)) {
        isNonStable(candidate.version)
      } else {
        false
      }
    }
  }

  afterEvaluate {
    tasks.withType<Test> {
      maxParallelForks =
        (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1).also {
          println("Setting maxParallelForks to $it")
        }
      testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events =
          EnumSet.of(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR,
          )
        exceptionFormat = TestExceptionFormat.FULL
      }
    }
  }
}

allprojects {
  afterEvaluate {
    extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension>()?.run {
      jvmToolchain {
        languageVersion = JavaLanguageVersion.of(rootProject.libs.versions.java.toolchain.get().toInt())
        vendor = JvmVendorSpec.AZUL
      }
    }
  }

  tasks.withType<KotlinCompile> {
    compilerOptions {
      jvmTarget = JvmTarget.fromTarget(javaTargetVersion.toString())
    }
  }

  val ktlintVersion = rootProject.libs.versions.ktlint.get()
  apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude(
        // Compose Multiplatform Res class
        "**/Res.kt",
        // Kotlin generated files
        "**/build/**/*.kt",
      )

      ktlint(ktlintVersion)

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")
      targetExclude("**/build/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")
      targetExclude("**/build/**/*.kts")

      ktlint(ktlintVersion)

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }
}
