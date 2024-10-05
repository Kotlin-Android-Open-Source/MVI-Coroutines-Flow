plugins {
  alias(libs.plugins.kotlin.jvm)
}

java {
  sourceCompatibility = javaTargetVersion
  targetCompatibility = javaTargetVersion
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(projects.core)
  api(libs.arrow.core)

  addUnitTest(project = project, testImplementation = false)
}
