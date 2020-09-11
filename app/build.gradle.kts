plugins {
  id("com.android.application")
  id("kotlin-android")
}

android {
  compileSdkVersion(appConfig.compileSdkVersion)
  buildToolsVersion(appConfig.buildToolsVersion)

  defaultConfig {
    applicationId = appConfig.applicationId
    minSdkVersion(appConfig.minSdkVersion)
    targetSdkVersion(appConfig.targetSdkVersion)
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
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
}

dependencies {
  implementation(
    fileTree(
      mapOf(
        "dir" to "libs",
        "include" to listOf("*.jar")
      )
    )
  )

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)
  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.material)

  // viewModelScope
  implementation(deps.lifecycle.viewModelKtx)

  // lifecycleScope
  implementation(deps.lifecycle.runtimeKtx)

  // retrofit2
  implementation(deps.squareup.retrofit)
  implementation(deps.squareup.converterMoshi)
  implementation(deps.squareup.loggingInterceptor)

  // moshi
  implementation(deps.squareup.moshiKotlin)

  // coroutines
  implementation(deps.jetbrains.coroutinesCore)
  implementation(deps.jetbrains.coroutinesAndroid)

  // koin
  implementation(deps.koinAndroidXViewModel)

  // coil
  implementation(deps.coil)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidxJunit)
  androidTestImplementation(deps.test.androidXSspresso)
}
