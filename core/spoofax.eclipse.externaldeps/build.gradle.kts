plugins {
  id("org.metaborg.gradle.config.java-library")
  id("biz.aQute.bnd.builder")
  id("org.metaborg.coronium.embedding")
}

// Add dependencies to JVM (non-OSGi) libraries. Must use `api` configuration for `coronium.embedding` plugin.
dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":common"))
  api(project(":spoofax.core"))

  api("org.metaborg:log.api")
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.runtime")
  api("org.metaborg:pie.dagger")

  api("org.metaborg:org.spoofax.terms")

  api("com.google.dagger:dagger")
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  "mb.*;provider=mb;mandatory:=provider",
  "org.spoofax.*;provider=mb;mandatory:=provider",
  "dagger.*;provider=mb;mandatory:=provider"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Require-Bundle", "javax.inject"), // Depends on javax.inject bundle provided by Eclipse.
        Pair("Export-Package", exports.joinToString(", ")),
        Pair("Import-Package", ""), // Disable imports
        Pair("Bundle-Version", embedding.bundleVersion),
        Pair("-nouses", "true"), // Disable 'uses' directive generation for exports.
        Pair("-nodefaultversion", "true") // Disable 'version' directive generation for exports.
      )
    }
  }
}
