plugins {
  androidLib
  kotlinAndroid
}

android {
  compileSdkVersion(appConfig.compileSdkVersion)
  buildToolsVersion(appConfig.buildToolsVersion)

  defaultConfig {
    minSdkVersion(appConfig.minSdkVersion)
    targetSdkVersion(appConfig.targetSdkVersion)
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
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
}

dependencies {
  implementation(deps.jetbrains.coroutinesCore)
  implementation(deps.jetbrains.coroutinesAndroid)

  implementation(deps.androidx.coreKtx)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.material)

  implementation(deps.lifecycle.commonJava8)
  implementation(deps.lifecycle.runtimeKtx)
}
