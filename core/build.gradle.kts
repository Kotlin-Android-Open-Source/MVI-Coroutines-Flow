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
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.coroutines.android)

  implementation(deps.androidx.coreKtx)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.material)

  implementation(deps.lifecycle.commonJava8)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.timber)

  addUnitTest()
}
