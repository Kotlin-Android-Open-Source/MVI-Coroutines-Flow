plugins {
  androidApplication
  kotlinAndroid
  jacoco
}

android {
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    applicationId = appConfig.applicationId
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

//    getByName("debug") {
//      isTestCoverageEnabled = true
//    }
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
  implementation(coreUi)
  implementation(featureMain)
  implementation(featureAdd)
  implementation(featureSearch)

  implementation(deps.coroutines.android)
  implementation(deps.koin.android)

  debugImplementation(deps.squareup.leakCanary)
  implementation(deps.timber)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidxJunit)
  androidTestImplementation(deps.test.androidXSspresso)

  addUnitTest()
  testImplementation(testUtils)
  testImplementation(deps.koin.testJunit4)
}
