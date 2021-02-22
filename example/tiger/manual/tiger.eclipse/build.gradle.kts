plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  bundleApi(compositeBuild("spoofax.eclipse"))

  bundleEmbedApi(project(":tiger"))
  bundleEmbedApi(project(":tiger.spoofax"))

  compileOnly("org.checkerframework:checker-qual-android")
  annotationProcessor("com.google.dagger:dagger-compiler")
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  // Provided by 'javax.inject' bundle.
  "!javax.inject.*",
  // Provided by 'spoofax.eclipse' bundle.
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
  // Allow split package for 'mb.nabl'.
  "mb.nabl2.*;-split-package:=merge-first",
  // Export packages from this project.
  "mb.tiger.eclipse.*",
  // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
  "*;provider=tiger;mandatory:=provider"
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
