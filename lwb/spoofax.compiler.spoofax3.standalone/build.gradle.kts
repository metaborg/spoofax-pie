plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  api(project(":spoofax.compiler.spoofax3"))
  api(project(":spoofax.compiler.spoofax3.dagger"))
  api(compositeBuild("spoofax.compiler.dagger"))
  api("org.metaborg:pie.task.java")

  // Using api configuration to make these annotations and processors available to javac that we call during
  // compilation, and to users of this library as well.
  api("org.checkerframework:checker-qual-android")
  api("com.google.dagger:dagger-compiler")

  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")

  testImplementation("org.junit.jupiter:junit-jupiter-params:${metaborg.junitVersion}")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation(project(":spoofax.compiler.spoofax3.standalone.dagger"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

tasks.test {
  // Show standard out and err in tests.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
