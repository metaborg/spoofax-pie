plugins {
  id("org.metaborg.gradle.config.java-library")
  id("biz.aQute.bnd.builder") version "4.1.0"
  id("org.metaborg.coronium.embedding")
}

// Add dependencies to JVM (non-OSGi) libraries. Must use `compileOnly` configuration for `coronium.embedding` plugin.
dependencies {
  api(platform(project(":depconstraints")))

  compileOnly(project(":common"))
  compileOnly(project(":spoofax.core"))

  compileOnly("org.metaborg:log.api")
  compileOnly("org.metaborg:resource")
  compileOnly("org.metaborg:pie.api")
  compileOnly("org.metaborg:pie.dagger")

  compileOnly("org.metaborg:org.spoofax.terms")

  compileOnly("com.google.dagger:dagger")
  compileOnly("javax.inject:javax.inject")
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  "mb.*",
  "org.metaborg.*",
  "org.spoofax.*",
  "dagger.*;provider=mb;mandatory:=provider",
  "javax.inject.*;provider=mb;mandatory:=provider"
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
