apply(plugin = "jacoco")

tasks {
  val debugCoverageReport by registering(JacocoReport::class)
  debugCoverageReport {
    dependsOn("testDebugUnitTest")

    reports {
      xml.run {
        required.value(true)
        outputLocation.set(file("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
      }
      html.required.value(true)
    }

    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/debug")
    val coverageSourceDirs = arrayOf(
      "src/main/java",
      "src/debug/java"
    )
    val executionDataDirs = fileTree("$buildDir") {
      setIncludes(
        listOf(
          "jacoco/testDebugUnitTest.exec",
          "outputs/code_coverage/debugAndroidTest/connected/*.ec",
          "outputs/code-coverage/connected/*coverage.ec"
        )
      )
    }

    classDirectories.setFrom(files(kotlinClasses))
    sourceDirectories.setFrom(coverageSourceDirs)
    additionalSourceDirs.setFrom(files(coverageSourceDirs))
    executionData.setFrom(executionDataDirs)
  }
}
