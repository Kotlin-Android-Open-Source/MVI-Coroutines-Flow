plugins {
  androidLib
  kotlinAndroid
}

android {
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
          value = it.toString(),
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
          value = it.toString(),
        )
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }
}

dependencies {
  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.coroutines.core)

  implementation(mviBase)
  implementation(testUtils)
  implementation(deps.timber)

  implementation(deps.arrow.core)

  addUnitTest(testImplementation = false)
}
