plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name:$version"

mavenize {
  majorVersion.set("2020-12")
}

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))


  bundleTargetPlatformApi(eclipse("javax.inject"))


  bundleApi(compositeBuild("spoofax.eclipse"))
  bundleApi(compositeBuild("tooling.eclipsebundle"))
  bundleApi(compositeBuild("spoofax.compiler.eclipsebundle"))


  bundleImplementation(project(":cfg.eclipse"))
  bundleImplementation(project(":sdf3.eclipse"))
  bundleImplementation(project(":esv.eclipse"))
  bundleImplementation(project(":stratego.eclipse"))
  bundleImplementation(project(":statix.eclipse"))

  bundleImplementation(project(":libspoofax2.eclipse"))
  bundleImplementation(project(":libstatix.eclipse"))


  // Convenient library to get the current classpath, which works under OSGi (Eclipse) as well. Used to pass the current
  // classpath to the Java compiler.
  // TODO: only using this to extract a classpath, can we just copy that functionality without a dependency?
  bundleEmbedImplementation("io.github.classgraph:classgraph:4.8.102")

  bundleEmbedImplementation("com.google.dagger:dagger-compiler") {
    // Exclude `checker-qual` in favor of `checker-qual-android` which has classfile retention instead of runtime.
    exclude("org.checkerframework", "checker-qual")
  }

  // HACK: embed javax.inject as classgraph does not seem to pick up the above javax.inject dependency?
  bundleEmbedImplementation("javax.inject:javax.inject:1")

  // Embed `:spoofax.lwb.dynamicloading`, which includes `:spoofax.lwb.compiler` and `:spoofax.lwb.compiler.dagger`.
  bundleEmbedImplementation(project(":spoofax.lwb.dynamicloading")) {
    // Exclude meta-languages and libraries, as they have their own Eclipse plugins
    exclude("org.metaborg", "cfg")
    exclude("org.metaborg", "esv")
    exclude("org.metaborg", "sdf3")
    exclude("org.metaborg", "stratego")
    exclude("org.metaborg", "statix")
    exclude("org.metaborg", "libspoofax2")
    exclude("org.metaborg", "libstatix")

    // Exclude modules already exported by `spoofax.eclipse`
    exclude("org.metaborg", "common")
    exclude("org.metaborg", "spoofax.core")
    exclude("org.metaborg", "log.api")
    exclude("org.metaborg", "resource")
    exclude("org.metaborg", "pie.api")
    exclude("org.metaborg", "pie.runtime")
    exclude("com.google.dagger", "dagger")

    // Exclude modules already exported by `tooling.eclipsebundle`
    exclude("org.metaborg", "spoofax.compiler.interfaces")

    // Exclude modules already exported by `spoofax.compiler.eclipsebundle`
    exclude("org.metaborg", "spoofax.compiler")
    exclude("org.metaborg", "spoofax.compiler.dagger")

    // Exclude dagger-compile because we already manually embed it.
    exclude("com.google.dagger", "dagger-compiler")
  }

  // Embed `org.metaborg:pie.task.archive` and `org.metaborg:pie.task.java`
  bundleEmbedImplementation("org.metaborg:pie.task.archive")
  bundleEmbedImplementation("org.metaborg:pie.task.java")
}

val privatePackage = listOf(
  // Our own packages should not be private.
  "!mb.spoofax.lwb.eclipse",
  "!mb.spoofax.lwb.eclipse.*",
  // Allow split packages for `mb.spoofax.lwb.compiler` because `spoofax.lwb.compiler.dagger` generates dagger classes
  // in the same package
  "mb.spoofax.lwb.compiler.*;-split-package:=merge-first",
  // Embed `mb.spoofax.lwb`, `mb.spoofax.lwb.dynamicloading`, and co.
  "mb.spoofax.lwb.*",
  // Embed `javax.inject`
  "javax.inject.*",
  // Embed `org.checkerframework:checker-qual-android`
  "org.checkerframework.*",
  // Embed `com.google.dagger:dagger-compiler`. Allow split packages for `dagger.model`, as `dagger-compiler` and
  // `dagger-spi` both have this package
  "dagger.model.*;-split-package:=merge-first",
  "dagger.*",
  // Embed `io.github.classgraph:classgraph`.
  "io.github.classgraph.*",
  "nonapi.io.github.classgraph.*",
  // Embed PIE task modules
  "mb.pie.task.archive.*",
  "mb.pie.task.java.*"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Private-Package", privatePackage.joinToString(", "))
      )
    }
  }
}
