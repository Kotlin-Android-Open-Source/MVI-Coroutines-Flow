plugins {
  kotlin
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.koin.core)
  implementation(deps.arrow.core)

  addUnitTest()
  testImplementation(testUtils)
}
