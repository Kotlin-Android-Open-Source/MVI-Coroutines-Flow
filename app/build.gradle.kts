plugins {
  alias(libs.plugins.android.app)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlinx.kover)
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
  }

  compileOptions {
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
  }

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

  implementation(projects.domain)
  implementation(projects.data)
  implementation(projects.core)
  implementation(projects.coreUi)
  implementation(projects.featureMain)
  implementation(projects.featureAdd)
  implementation(projects.featureSearch)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.koin.android)
  implementation(libs.androidx.material)
  implementation(libs.androidx.startup)

  debugImplementation(libs.squareup.leakcanary)
  implementation(libs.timber)
  implementation(libs.viewBindingDelegate)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.junit.ktx)
  androidTestImplementation(libs.androidx.test.core.ktx)
  androidTestImplementation(libs.androidx.test.espresso.core)

  addUnitTest(project = project)
  testImplementation(projects.testUtils)
  testImplementation(libs.koin.test.junit4)
  testImplementation(libs.koin.test)
}

dependencies {
  kover(projects.featureMain)
  kover(projects.featureAdd)
  kover(projects.featureSearch)
  kover(projects.domain)
  kover(projects.data)
  kover(projects.core)
  kover(projects.coreUi)
  kover(projects.mviBase)
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
