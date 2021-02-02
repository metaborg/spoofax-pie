plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("spoofax.core"))
  api(project(":spoofax.compiler.spoofax3.standalone"))
  api(project(":spoofax.compiler.spoofax3.standalone.dagger"))

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation("org.slf4j:slf4j-nop:1.7.30")
}

//tasks.test {
//  // Show standard out and err in tests.
//  testLogging {
//    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
//    showStandardStreams = true
//  }
//}
