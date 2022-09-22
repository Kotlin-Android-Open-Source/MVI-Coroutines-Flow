plugins {
  androidLib
  kotlinAndroid
}

android {
  namespace = "com.hoc.flowmvi.mvi_testing"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    debug {
      (!isCiBuild).let {
        buildConfigField(
          type = it::class.java.simpleName,
          name = "ENABLE_LOG_TEST",
          value = it.toString()
        )
      }
    }
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      false.let {
        buildConfigField(
          type = it::class.java.simpleName,
          name = "ENABLE_LOG_TEST",
          value = it.toString()
        )
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = JavaVersion.VERSION_11.toString() }
}

dependencies {
  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.coroutines.core)

  implementation(mviBase)
  api(testUtils)
  implementation(deps.timber)

  implementation(deps.arrow.core)

  addUnitTest(testImplementation = false)
}
