plugins {
  androidApplication
  kotlinAndroid
  id("org.jetbrains.kotlinx.kover")
}

android {
  namespace = "com.hoc.flowmvi"
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
        "proguard-rules.pro",
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

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }

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
        "include" to listOf("*.jar"),
      ),
    ),
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
  implementation(deps.androidx.material)
  implementation(deps.androidx.startup)

  debugImplementation(deps.squareup.leakCanary)
  implementation(deps.timber)
  implementation(deps.viewBindingDelegate)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidx.junit)
  androidTestImplementation(deps.test.androidx.core)
  androidTestImplementation(
    deps
      .test
      .androidx
      .espresso
      .core,
  )

  addUnitTest()
  testImplementation(testUtils)
  testImplementation(deps.koin.testJunit4)
  testImplementation(deps.koin.test)
}

dependencies {
  kover(project(":feature-main"))
  kover(project(":feature-add"))
  kover(project(":feature-search"))
  kover(project(":domain"))
  kover(project(":data"))
  kover(project(":core"))
  kover(project(":core-ui"))
  kover(project(":mvi-base"))
}

kover {
  reports {
    // filters for all report types of all build variants
    filters {
      excludes {
        classes(
          "*.databinding.*",
          "*.BuildConfig",
        )
      }
    }
  }
}
