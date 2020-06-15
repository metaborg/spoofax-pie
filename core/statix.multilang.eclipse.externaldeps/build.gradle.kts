plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  bundleTargetPlatformApi(eclipse("javax.inject"))

  bundleEmbedApi(project(":common"))
  bundleEmbedApi(project(":statix.multilang"))

  bundleEmbedApi("org.metaborg:log.api")
  bundleEmbedApi("org.metaborg:resource")
  bundleEmbedApi("org.metaborg:pie.api")
  bundleEmbedApi("org.metaborg:pie.runtime")
  bundleEmbedApi("org.metaborg:pie.dagger")

  bundleEmbedApi("com.google.dagger:dagger")
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
  "mb.*;provider=mb;mandatory:=provider",
  "org.spoofax.*;provider=mb;mandatory:=provider",
  "dagger;provider=mb;mandatory:=provider",
  "dagger.*;provider=mb;mandatory:=provider"
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
