plugins {
  androidLib
  kotlinAndroid
  kotlinKapt
}

android {
  namespace = "com.hoc.flowmvi.data"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = JavaVersion.VERSION_11.toString() }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(core)
  implementation(domain)

  implementation(deps.coroutines.core)
  implementation(deps.flowExt)

  implementation(deps.squareup.retrofit)
  implementation(deps.squareup.moshiKotlin)
  implementation(deps.squareup.converterMoshi)
  implementation(deps.squareup.loggingInterceptor)

  implementation(deps.koin.core)
  implementation(deps.arrow.core)

  implementation(deps.timber)

  addUnitTest()
  testImplementation(testUtils)
  testImplementation(deps.koin.testJunit4)
  testImplementation(deps.koin.test)
}
