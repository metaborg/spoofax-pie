plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

mavenize {
  majorVersion.set("2021-03")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  bundleTargetPlatformApi(eclipse("javax.inject"))

  bundleApi(project(":spoofax.eclipse"))

  bundleEmbedApi(project(":aterm.common"))
  bundleEmbedApi(project(":constraint.common"))
  bundleEmbedApi(project(":constraint.pie"))
  bundleEmbedApi(project(":esv.common"))
  bundleEmbedApi(project(":jsglr.common"))
  bundleEmbedApi(project(":jsglr.pie"))
  bundleEmbedApi(project(":jsglr1.common"))
  bundleEmbedApi(project(":jsglr2.common"))
  bundleEmbedApi(project(":nabl2.common"))
  bundleEmbedApi(project(":spoofax2.common"))
  bundleEmbedApi(project(":statix.common"))
  bundleEmbedApi(project(":statix.pie"))
  bundleEmbedApi(project(":statix.codecompletion"))
  bundleEmbedApi(project(":statix.codecompletion.pie"))
  bundleEmbedApi(project(":stratego.common"))
  bundleEmbedApi(project(":stratego.pie"))
  bundleEmbedApi(project(":spt.api"))
  bundleEmbedApi(project(":tego"))
  bundleEmbedApi(project(":tego.pie"))

  bundleEmbedApi(project(":spoofax.compiler.interfaces"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exportPackage = listOf(
  // Provided by `javax.inject`
  "!javax.inject.*",
  // Provided by `:spoofax.eclipse`
  "!mb.log.*",
  "!mb.resource.*",
  "!mb.pie.api.*",
  "!mb.pie.runtime.*",
  "!mb.common.*",
  "!mb.spoofax.core.*",
  "!dagger.*",
  // Do not export testing packages.
  "!junit.*",
  "!org.junit.*",
  // Do not export compile-time annotation packages.
  "!org.checkerframework.*",
  "!org.codehaus.mojo.animal_sniffer.*",
  // Allow split package for 'mb.nabl2'.
  "mb.nabl2.*;-split-package:=merge-first",
  // Export our own package
  "mb.tooling.eclipsebundle",
  // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
  "*;provider=tooling.eclipsebundle;mandatory:=provider"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Export-Package", exportPackage.joinToString(", "))
      )
    }
  }
}
