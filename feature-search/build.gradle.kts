plugins {
  androidLib
  kotlinAndroid
  id("dev.ahmedmourad.nocopy.nocopy-gradle-plugin")
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
  buildFeatures { viewBinding = true }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(domain)
  implementation(core)
  implementation(mviBase)

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.material)

  implementation(deps.coroutines.core)
  implementation(deps.koin.android)
  implementation(deps.coil)
  implementation(deps.viewBindingDelegate)
  implementation(deps.flowExt)

  addUnitTest()
}
