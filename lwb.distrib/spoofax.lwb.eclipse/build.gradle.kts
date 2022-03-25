plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name:$version"

mavenize {
  majorVersion.set("2021-03")
}

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))


  bundleTargetPlatformApi(eclipse("javax.inject"))
  bundleTargetPlatformApi(eclipse("org.eclipse.jdt.core"))


  bundleApi(compositeBuild("spoofax.eclipse"))
  bundleApi(compositeBuild("tooling.eclipsebundle"))
  bundleApi(compositeBuild("spoofax.compiler.eclipsebundle"))


  bundleImplementation(compositeBuild("cfg.eclipse"))
  bundleImplementation(compositeBuild("sdf3.eclipse"))
  bundleImplementation(compositeBuild("esv.eclipse"))
  bundleImplementation(compositeBuild("stratego.eclipse"))
  bundleImplementation(compositeBuild("statix.eclipse"))
  bundleImplementation(compositeBuild("dynamix.eclipse"))
  bundleImplementation(compositeBuild("dynamix_runtime.eclipse"))
  bundleImplementation(compositeBuild("tim.eclipse"))
  bundleImplementation(compositeBuild("sdf3_ext_statix.eclipse"))
  bundleImplementation(compositeBuild("sdf3_ext_dynamix.eclipse"))
  bundleImplementation(compositeBuild("spt.eclipse"))

  bundleImplementation(compositeBuild("strategolib.eclipse"))
  bundleImplementation(compositeBuild("gpp.eclipse"))
  bundleImplementation(compositeBuild("libspoofax2.eclipse"))
  bundleImplementation(compositeBuild("libstatix.eclipse"))

  bundleImplementation(project(":rv32im.eclipse"))


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

  // Embed `:spoofax.lwb.dynamicloading`.
  bundleEmbedImplementation(compositeBuild("spoofax.lwb.dynamicloading")) {
    // Exclude modules already exported by `spoofax.eclipse`
    exclude("org.metaborg", "common")
    exclude("org.metaborg", "spoofax.core")
    exclude("org.metaborg", "log.api")
    exclude("org.metaborg", "resource")
    exclude("org.metaborg", "pie.api")
    exclude("org.metaborg", "pie.runtime")
    exclude("com.google.dagger", "dagger")

    // Exclude dagger-compile because we already manually embed it.
    exclude("com.google.dagger", "dagger-compiler")
  }

  // Embed `:spoofax.lwb.compiler`.
  bundleEmbedImplementation(compositeBuild("spoofax.lwb.compiler")) {
    // Exclude meta-languages and libraries, as they have their own Eclipse plugins
    exclude("org.metaborg", "cfg")
    exclude("org.metaborg", "sdf3")
    exclude("org.metaborg", "esv")
    exclude("org.metaborg", "stratego")
    exclude("org.metaborg", "statix")
    exclude("org.metaborg", "sdf3_ext_statix")
    exclude("org.metaborg", "spt")

    exclude("org.metaborg", "strategolib")
    exclude("org.metaborg", "gpp")
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

    // Exclude dagger-compile because we already manually embed it.
    exclude("com.google.dagger", "dagger-compiler")
  }

  // Embed `:spt.dynamicloading`.
  bundleEmbedImplementation(compositeBuild("spt.dynamicloading"))

  // Embed `org.metaborg:pie.task.archive` and `org.metaborg:pie.task.java`
  bundleEmbedImplementation("org.metaborg:pie.task.archive")
  bundleEmbedImplementation("org.metaborg:pie.task.java")

  // Embed FST serialize/deserialize implementation.
  bundleEmbedImplementation("org.metaborg:pie.serde.fst")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
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
  // Embed `mb.spt.dynamicloading`
  "mb.spt.dynamicloading.*",
  // Embed `javax.inject`
  "javax.inject.*",
  // Embed `org.checkerframework:checker-qual-android`
  "org.checkerframework.*",
  // Embed `com.google.dagger:dagger-compiler` and dependencies. Allow split packages for `dagger.model`, as
  // `dagger-compiler` and `dagger-spi` both have this package
  "dagger.model.*;-split-package:=merge-first",
  "dagger.*",
  "dagger.internal.*",
  "com.squareup.javapoet.*",
  "kotlin.*;-split-package:=merge-first",
  "javax.annotation.*;-split-package:=merge-first",
  // Embed `io.github.classgraph:classgraph`.
  "io.github.classgraph.*",
  "nonapi.io.github.classgraph.*",
  // Embed PIE task modules
  "mb.pie.task.archive.*",
  "mb.pie.task.java.*",
  // Embed PIE FST Serde, FST, and its dependencies.
  "mb.pie.serde.fst.*",
  "org.nustaq.*",
  "com.fasterxml.jackson.core.*",
  "org.objenesis.*",
  // Embed services, to make the embedded Dagger annotation processor work.
  "META-INF.services.*;-split-package:=merge-first"
)
tasks {
  "jar"(Jar::class) {
    manifest {
      attributes(
        Pair("Private-Package", privatePackage.joinToString(", "))
      )
    }
  }
  withType<mb.coronium.task.EclipseRun> {
    jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
  }
}
