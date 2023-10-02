import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.EnumSet
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
  id("org.jetbrains.kotlinx.kover") version "0.7.3" apply false
  id("com.diffplug.spotless") version "6.22.0" apply false
}

buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.1.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:6.20.0")
    classpath("dev.drewhamilton.poko:poko-gradle-plugin:0.15.0")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.46.0")
  }
}

subprojects {
  apply(plugin = "com.github.ben-manes.versions")

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
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      val version = JavaVersion.VERSION_11.toString()
      jvmTarget = version
    }
  }

  apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")

      ktlint(ktlintVersion)

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")

      ktlint(ktlintVersion)

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }
}
