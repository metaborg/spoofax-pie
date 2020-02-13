import mb.spoofax.gradle.util.configureSpoofaxLanguageArtifact
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

repositories {
  maven { url = uri("https://repo.gradle.org/gradle/libs-releases") } // Required for Gradle tooling API.
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":common"))
  api(project(":spoofax.core"))

  api("com.samskivert:jmustache:1.15")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")

  testImplementation("org.junit.jupiter:junit-jupiter-params:${metaborg.junitVersion}")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testImplementation("org.eclipse.jdt:org.eclipse.jdt.core:3.19.0")
  testImplementation("org.gradle:gradle-tooling-api:5.6.4")
  testRuntimeOnly("org.slf4j:slf4j-simple:1.7.10") // SLF4J implementation required for Gradle tooling API.
  testCompileOnly("org.checkerframework:checker-qual-android")
}

tasks.test {
  // Show standard out and err in tests, to ensure that failed Gradle builds are properly reported.
  testLogging {
    events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
