plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  bundleTargetPlatformApi(eclipse("javax.inject"))
  bundleApi(project(":spoofax.eclipse.externaldeps"))

  bundleEmbedApi(project(":common"))
  bundleEmbedApi(project(":statix.multilang"))

  bundleEmbedApi("org.metaborg:log.api")
  bundleEmbedApi("org.metaborg:resource")
  bundleEmbedApi("org.metaborg:pie.api")
  bundleEmbedApi("org.metaborg:pie.runtime")
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  // Provided by 'javax.inject' bundle.
  "!javax.inject.*",
  // Provided by 'spoofax.eclipse.externaldeps' bundle.
  "!mb.log.*",
  "!mb.resource.*",
  "!mb.pie.*",
  "!mb.common.*",
  "!mb.spoofax.core.*",
  // Do not export compile-time annotation packages.
  "!org.checkerframework.*",
  "!org.codehaus.mojo.animal_sniffer.*",
  // Allow split package for 'mb.nabl2' and 'mb.statix'.
  "mb.nabl2.*;-split-package:=merge-first",
  "mb.statix.*;-split-package:=merge-first",
  // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
  "*;provider=statix;mandatory:=provider"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Export-Package", exports.joinToString(", "))
      )
    }
  }
}
