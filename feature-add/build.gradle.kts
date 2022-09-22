plugins {
  androidLib
  kotlinAndroid
  kotlinParcelize
}

android {
  namespace = "com.hoc.flowmvi.ui.add"
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
  buildFeatures { viewBinding = true }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(domain)
  implementation(core)
  implementation(coreUi)
  implementation(mviBase)

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.material)

  implementation(deps.coroutines.core)
  implementation(deps.koin.android)
  implementation(deps.arrow.core)

  implementation(deps.viewBindingDelegate)
  implementation(deps.flowExt)
  implementation(deps.timber)

  addUnitTest()
  testImplementation(mviTesting)
}
