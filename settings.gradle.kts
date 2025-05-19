enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
}

val copyToBuildSrc = { sourcePath: String ->
  rootDir.resolve(sourcePath).copyRecursively(
    target = rootDir.resolve("buildSrc").resolve(sourcePath),
    overwrite = true,
  )
  println("[DONE] copied $sourcePath")
}
arrayOf("gradle.properties", "gradle/wrapper").forEach(copyToBuildSrc)

rootProject.name = "MVI-Coroutines-Flow"
include(":app")
include(":feature-main")
include(":feature-add")
include(":feature-search")
include(":domain")
include(":data")
include(":core")
include(":core-ui")
include(":test-utils")
includeProject(":mvi-base", "mvi/mvi-base")
includeProject(":mvi-testing", "mvi/mvi-testing")

fun includeProject(
  name: String,
  filePath: String,
) {
  include(name)
  project(name).projectDir = File(filePath)
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}
