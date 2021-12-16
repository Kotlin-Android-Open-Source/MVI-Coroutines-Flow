plugins {
  kotlin
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(core)
  api(deps.arrow.core)

  addUnitTest(testImplementation = false)
}
