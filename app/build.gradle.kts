plugins {
  androidApplication
  kotlinAndroid
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

  implementation(domain)
  implementation(data)
  implementation(core)
  implementation(featureMain)
  implementation(featureAdd)

  implementation(deps.jetbrains.coroutinesAndroid)
  implementation(deps.koin.android)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidxJunit)
  androidTestImplementation(deps.test.androidXSspresso)
}
