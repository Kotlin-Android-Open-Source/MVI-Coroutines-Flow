plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.kotlinx.kover)
}

android {
  namespace = "com.hoc.flowmvi.data"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  compileOptions {
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(projects.core)
  implementation(projects.domain)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.flowExt)

  implementation(libs.squareup.retrofit)
  implementation(libs.squareup.moshi.kotlin)
  implementation(libs.squareup.retrofit.converter.moshi)
  implementation(libs.squareup.logging.interceptor)

  implementation(libs.koin.core)
  implementation(libs.arrow.core)

  implementation(libs.timber)

  addUnitTest(project = project)
  testImplementation(projects.testUtils)
  testImplementation(libs.koin.test.junit4)
  testImplementation(libs.koin.test)
}
