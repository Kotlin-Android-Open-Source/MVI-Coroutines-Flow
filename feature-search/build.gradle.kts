plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.poko)
  alias(libs.plugins.kotlinx.kover)
}

android {
  namespace = "com.hoc.flowmvi.ui.search"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
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

  buildFeatures { viewBinding = true }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(projects.domain)
  implementation(projects.core)
  implementation(projects.coreUi)
  implementation(projects.mviBase)

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)

  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.swiperefreshlayout)
  implementation(libs.androidx.material)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.coil)
  implementation(libs.viewBindingDelegate)
  implementation(libs.flowExt)
  implementation(libs.arrow.core)
  implementation(libs.timber)

  addUnitTest(project = project)
  testImplementation(projects.mviTesting)
}
