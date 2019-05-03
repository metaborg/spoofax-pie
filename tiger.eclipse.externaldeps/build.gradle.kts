plugins {
  id("org.metaborg.gradle.config.java-library")
  id("biz.aQute.bnd.builder") version "4.1.0"
  id("org.metaborg.coronium.embedding")
}

// Add dependencies to JVM (non-OSGi) libraries. Must use `api` configuration for `coronium.embedding` plugin.
dependencies {
  api(platform(project(":depconstraints")))

  api(project(":tiger"))
  api(project(":tiger.spoofax"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val requires = listOf(
  "javax.inject", // Depends on javax.inject bundle provided by Eclipse.
  "spoofax.eclipse.externaldeps" // Depends on external dependencies from spoofax.eclipse.
)
val exports = listOf(
  // Provided by 'javax.inject' bundle.
  "!javax.inject.*",
  // Provided by 'spoofax.eclipse.externaldeps' bundle.
  "!mb.log.*",
  "!mb.resource.*",
  "!mb.pie.*",
  "!mb.common.*",
  "!mb.spoofax.core.*",
  "!dagger.*",
  // Do not export testing packages.
  "!junit.*",
  "!org.junit.*",
  // Do not export compile-time annotation packages.
  "!org.checkerframework.*",
  "!org.codehaus.mojo.animal_sniffer.*",
  // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
  "*;provider=tiger;mandatory:=provider"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Bundle-Vendor", project.group),
        Pair("Bundle-SymbolicName", project.name),
        Pair("Bundle-Name", project.name),
        Pair("Bundle-Version", embedding.bundleVersion),

        Pair("Require-Bundle", requires.joinToString(", ")),
        Pair("Import-Package", ""), // Disable imports

        Pair("Export-Package", exports.joinToString(", ")),

        Pair("-nouses", "true"), // Disable 'uses' directive generation for exports.
        Pair("-nodefaultversion", "true") // Disable 'version' directive generation for exports.
      )
    }
  }
}
