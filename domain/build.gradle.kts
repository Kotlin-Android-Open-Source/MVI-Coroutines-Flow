plugins {
  kotlin
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.koin.core)
  implementation(deps.arrow.core)

  addUnitTest()
  testImplementation(testUtils)
}
