rootProject.name = "MVI Coroutines Flow"

val copyToBuildSrc = { sourcePath: String ->
  rootDir.resolve(sourcePath).copyRecursively(
    target = rootDir.resolve("buildSrc").resolve(sourcePath),
    overwrite = true
  )
  println("[DONE] copied $sourcePath")
}
arrayOf("gradle.properties", "gradle").forEach(copyToBuildSrc)

include(":app")
include(":feature-main")
include(":feature-add")
include(":feature-search")
include(":domain")
include(":data")
include(":core")
include(":core-ui")
include(":test-utils")
includeProject(":mvi-base", "mvi/mvi-base")
includeProject(":mvi-testing", "mvi/mvi-testing")

fun includeProject(name: String, filePath: String) {
  include(name)
  project(name).projectDir = File(filePath)
}
