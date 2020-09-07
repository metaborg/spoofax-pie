import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.util.*

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
  testAnnotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":common"))
  api(project(":spoofax.core"))
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("com.google.dagger:dagger")

  api("com.samskivert:jmustache:1.15")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
//  annotationProcessor("com.google.dagger:dagger-compiler")

  testImplementation("org.junit.jupiter:junit-jupiter-params:${metaborg.junitVersion}")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testImplementation("org.eclipse.jdt:org.eclipse.jdt.core:3.19.0")
  testImplementation("org.gradle:gradle-tooling-api:5.6.4")
  testRuntimeOnly("org.slf4j:slf4j-simple:1.7.10") // SLF4J implementation required for Gradle tooling API.
  testCompileOnly("org.checkerframework:checker-qual-android")
  //testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

tasks.test {
  // Show standard out and err in tests, to ensure that failed Gradle builds are properly reported.
  testLogging {
    events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}

// Add generated resources directory as a resource source directory.
val generatedResourcesDir = project.buildDir.resolve("generated/resources")
sourceSets {
  main {
    resources {
      srcDir(generatedResourcesDir)
    }
  }
}

tasks.register<CompileJava>("daggerCompileJava") {

}

// Task that writes (dependency) versions to a versions.properties file, which is used in the Shared class.
val versionsPropertiesFile = generatedResourcesDir.resolve("version.properties")
val generateVersionPropertiesTask = tasks.register("generateVersionProperties") {
  inputs.property("version", project.version.toString())
  outputs.file(versionsPropertiesFile)
  doLast {
    val properties = NonShittyProperties()
    properties.setProperty("spoofax3", project.version.toString())
    versionsPropertiesFile.ensureParentDirsCreated()
    versionsPropertiesFile.bufferedWriter().use {
      properties.storeWithoutDate(it)
    }
  }
}
tasks.compileJava.configure { dependsOn(generateVersionPropertiesTask) }
tasks.compileTestJava.configure { dependsOn(generateVersionPropertiesTask) }

// Custom properties class that does not write the current date, fixing incrementality.
class NonShittyProperties : Properties() {
  fun storeWithoutDate(writer: java.io.BufferedWriter) {
    val e: Enumeration<*> = keys()
    while(e.hasMoreElements()) {
      val key = e.nextElement()
      val value = get(key)
      writer.write("$key=$value")
      writer.newLine()
    }
    writer.flush()
  }
}
