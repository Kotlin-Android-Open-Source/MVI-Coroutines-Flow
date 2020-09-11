// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    google()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.0.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
