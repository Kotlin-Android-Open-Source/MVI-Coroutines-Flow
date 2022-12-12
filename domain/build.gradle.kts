plugins {
  kotlin
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.koin.core)
  implementation(deps.arrow.core)

  implementation(core)

  addUnitTest()
  testImplementation(testUtils)
}
