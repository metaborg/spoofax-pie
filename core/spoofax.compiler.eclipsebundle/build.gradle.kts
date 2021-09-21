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
  bundleApi(project(":tooling.eclipsebundle"))

  bundleEmbedApi(project(":spoofax.compiler"))
  bundleEmbedApi(project(":spoofax.compiler.dagger"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exportPackage = listOf(
  // Already provided by `:tooling.eclipsebundle`
  "!mb.spoofax.compiler.interfaces.*",
  // Export `:spoofax.compiler` and `:spoofax.compiler.dagger`. Allow split package because `:spoofax.compiler.dagger`
  // generates dagger classes in the same package
  "mb.spoofax.compiler.*;-split-package:=merge-first",
  // Export `com.samskivert:jmustache` and `CachingMustacheCompiler` from `:spoofax.compiler`. Allow split package
  // because of the `CachingMustacheCompiler` class
  "com.samskivert.mustache.*;-split-package:=merge-first"
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
