plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "com.hoc.flowmvi.mvi_testing"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    debug {
      (!isCiBuild).let { enabledLogTest ->
        buildConfigField(
          type = enabledLogTest::class.java.simpleName,
          name = "ENABLE_LOG_TEST",
          value = enabledLogTest.toString(),
        )
      }
    }
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
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
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.kotlinx.coroutines.core)

  implementation(projects.mviBase)
  api(projects.testUtils)
  implementation(libs.timber)

  implementation(libs.arrow.core)

  addUnitTest(project = project, testImplementation = false)
}
