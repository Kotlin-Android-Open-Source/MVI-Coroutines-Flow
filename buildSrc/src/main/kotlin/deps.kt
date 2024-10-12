import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

val Project.javaTargetVersion: JavaVersion
  get() = JavaVersion.toVersion(libsVersionCatalog.version("java.target").toString().toInt())

val Project.isCiBuild: Boolean
  get() = providers.environmentVariable("CI").orNull == "true"

fun DependencyHandler.addUnitTest(
  project: Project,
  testImplementation: Boolean = true,
) {
  val configName = if (testImplementation) "testImplementation" else "implementation"
  val libs = project.libsVersionCatalog

  add(configName, libs["junit"])
  add(configName, libs["mockk"])
  add(configName, libs["kotlin.test.junit"])
  add(configName, libs["kotlinx.coroutines.test"])
}
