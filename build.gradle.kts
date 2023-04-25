import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.EnumSet
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.4.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:6.18.0")
    classpath("dev.drewhamilton.poko:poko-gradle-plugin:0.13.0")
    classpath("org.jacoco:org.jacoco.core:0.8.10")
    classpath("com.vanniktech:gradle-android-junit-jacoco-plugin:0.17.0-SNAPSHOT")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.46.0")
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "com.vanniktech.android.junit.jacoco")

  apply(plugin = "com.github.ben-manes.versions")

  fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
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

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    val EDITOR_CONFIG_KEYS: Set<String> = hashSetOf(
      "ij_kotlin_imports_layout",
      "indent_size",
      "end_of_line",
      "charset",
      "disabled_rules"
    )

    kotlin {
      target("**/*.kt")

      // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
      val data = mapOf(
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8",
        "disabled_rules" to arrayOf(
          "package-name",
          "trailing-comma",
          "filename",
          "experimental:type-parameter-list-spacing",
        ).joinToString(separator = ","),
      )

      ktlint(ktlintVersion)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
        .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

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

      val data = mapOf(
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8"
      )
      ktlint(ktlintVersion)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
        .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }

  configure<com.vanniktech.android.junit.jacoco.JunitJacocoExtension> {
    jacocoVersion = "0.8.8"
    includeNoLocationClasses = true
    includeInstrumentationCoverageInMergedReport = true
    csv.isEnabled = false
    xml.isEnabled = true
    html.isEnabled = true
  }

  afterEvaluate {
    tasks.withType<Test> {
      extensions
        .getByType<JacocoTaskExtension>()
        .run {
          isIncludeNoLocationClasses = true
          excludes = listOf("jdk.internal.*")
        }

      maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1).also {
        println("Setting maxParallelForks to $it")
      }
      testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events = EnumSet.of(
          TestLogEvent.PASSED,
          TestLogEvent.FAILED,
          TestLogEvent.SKIPPED,
          TestLogEvent.STANDARD_OUT,
          TestLogEvent.STANDARD_ERROR
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
      languageVersion = "1.8"
    }
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
