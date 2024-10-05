plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlinx.kover)
}

java {
  sourceCompatibility = javaTargetVersion
  targetCompatibility = javaTargetVersion
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.arrow.core)

  implementation(projects.core)

  addUnitTest(project = project)
  testImplementation(projects.testUtils)
}
