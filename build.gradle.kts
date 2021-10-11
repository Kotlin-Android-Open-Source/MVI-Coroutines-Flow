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
    classpath("com.android.tools.build:gradle:7.0.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:5.16.0")
    classpath("dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.4.0")
    classpath("org.jacoco:org.jacoco.core:0.8.7")
    classpath("com.vanniktech:gradle-android-junit-jacoco-plugin:0.17.0-SNAPSHOT")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.39.0")
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "com.vanniktech.android.junit.jacoco")
  apply(plugin = "com.github.ben-manes.versions")

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")

      ktlint(ktlintVersion).userData(
        // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
        mapOf(
          "indent_size" to "2",
          "kotlin_imports_layout" to "ascii"
        )
      )

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

      ktlint(ktlintVersion).userData(
        mapOf(
          "indent_size" to "2",
          "kotlin_imports_layout" to "ascii"
        )
      )

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }

  configure<com.vanniktech.android.junit.jacoco.JunitJacocoExtension> {
    jacocoVersion = "0.8.7"
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
    }
  }
}

allprojects {
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      val version = JavaVersion.VERSION_1_8.toString()
      jvmTarget = version
      sourceCompatibility = version
      targetCompatibility = version
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
