plugins {
  kotlin
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(core)

  addUnitTest(testImplementation = false)
}
