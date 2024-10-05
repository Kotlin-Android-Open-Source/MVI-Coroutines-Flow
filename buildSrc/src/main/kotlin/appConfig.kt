@Suppress("ConstPropertyName", "ClassName")
object appConfig {
  const val applicationId = "com.hoc.flowmvi"

  const val compileSdkVersion = 34
  const val buildToolsVersion = "34.0.0"
  const val minSdkVersion = 21
  const val targetSdkVersion = 34

  private const val MAJOR = 2
  private const val MINOR = 2
  private const val PATCH = 0
  const val versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
  const val versionName = "$MAJOR.$MINOR.$PATCH"
}
