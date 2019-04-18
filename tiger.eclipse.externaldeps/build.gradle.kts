plugins {
  id("org.metaborg.gradle.config.java-library")
  id("biz.aQute.bnd.builder") version "4.1.0"
  id("org.metaborg.coronium.embedding")
}

// Add dependencies to JVM (non-OSGi) libraries. Must use `compileOnly` configuration for `coronium.embedding` plugin.
dependencies {
  api(platform(project(":depconstraints")))

  compileOnly(project(":tiger"))
  compileOnly(project(":tiger.spoofax"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  "mb.tiger.*"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(Pair("Export-Package", exports.joinToString(", ")))
      attributes(Pair("Import-Package", "")) // No imports needed
      attributes(Pair("Bundle-Version", embedding.bundleVersion))
    }
  }
}
