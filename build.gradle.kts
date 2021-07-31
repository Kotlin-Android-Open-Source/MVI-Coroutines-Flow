import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:5.14.2")
    classpath("dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.4.0")
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")

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
