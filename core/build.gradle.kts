plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlinx.kover)
}

java {
  sourceCompatibility = javaTargetVersion
  targetCompatibility = javaTargetVersion
}

dependencies {
  api(libs.kotlinx.coroutines.core)
  api(libs.arrow.core)
  addUnitTest(project = project)
}
