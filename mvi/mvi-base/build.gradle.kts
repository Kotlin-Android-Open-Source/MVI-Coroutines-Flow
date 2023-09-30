plugins {
  androidLib
  kotlinAndroid
  id("org.jetbrains.kotlinx.kover")
}

android {
  namespace = "com.hoc.flowmvi.mvi_base"
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
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = JavaVersion.VERSION_11.toString() }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(deps.androidx.appCompat)
  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)
  implementation(deps.coroutines.core)

  implementation(coreUi)
  implementation(core)
  implementation(deps.timber)

  addUnitTest()
}
