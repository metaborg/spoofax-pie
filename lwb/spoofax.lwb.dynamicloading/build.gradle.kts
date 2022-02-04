plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("spoofax.core"))
  api("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")

  testImplementation("org.slf4j:slf4j-nop:1.7.30")
  testImplementation(project(":spt"))
  testImplementation(project(":spt.dynamicloading"))
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.serde.fst")
}

tasks.test {
  enableAssertions = false // HACK: disable assertions due to assertion in the Stratego compiler.
  // Show stderr in tests.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
  }
  jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
}
