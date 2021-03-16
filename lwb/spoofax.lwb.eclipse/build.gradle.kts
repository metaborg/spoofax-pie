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

  bundleImplementation(compositeBuild("spoofax.eclipse"))

  bundleImplementation(project(":sdf3.eclipse")) // HACK: put SDF3 first for testing SDF3 config injection.
  bundleImplementation(project(":cfg.eclipse"))
  bundleImplementation(project(":esv.eclipse"))
  bundleImplementation(project(":stratego.eclipse"))
  bundleImplementation(project(":statix.eclipse"))

  bundleImplementation(project(":libspoofax2.eclipse"))
  bundleImplementation(project(":libstatix.eclipse"))

  // HACK: embed javax.inject as classgraph does not seem to pick up the above javax.inject dependency.
  bundleEmbedImplementation("javax.inject:javax.inject:1")

  // Embed `spoofax.lwb.dynamicloading`, which includes `spoofax.lwb.compiler` and co.
  bundleEmbedImplementation(project(":spoofax.lwb.dynamicloading")) {
    // Exclude meta-languages and libraries, as they have their own Eclipse plugins.
    exclude("org.metaborg", "cfg")
    exclude("org.metaborg", "esv")
    exclude("org.metaborg", "sdf3")
    exclude("org.metaborg", "stratego")
    exclude("org.metaborg", "statix")
    exclude("org.metaborg", "libspoofax2")
    exclude("org.metaborg", "libstatix")

    // Exclude modules already exported by `spoofax.eclipse`.
    exclude("org.metaborg", "common")
    exclude("org.metaborg", "spoofax.core")
    exclude("org.metaborg", "log.api")
    exclude("org.metaborg", "resource")
    exclude("org.metaborg", "pie.api")
    exclude("org.metaborg", "pie.runtime")
    exclude("org.metaborg.devenv", "org.spoofax.terms")
    exclude("com.google.dagger", "dagger")
  }
  bundleEmbedImplementation("org.metaborg:pie.task.archive")
  bundleEmbedImplementation("org.metaborg:pie.task.java")

  bundleEmbedImplementation(compositeBuild("constraint.common"))
  bundleEmbedImplementation(compositeBuild("constraint.pie"))
  bundleEmbedImplementation(compositeBuild("esv.common"))
  bundleEmbedImplementation(compositeBuild("jsglr.common"))
  bundleEmbedImplementation(compositeBuild("jsglr1.common"))
  bundleEmbedImplementation(compositeBuild("jsglr1.pie"))
  bundleEmbedImplementation(compositeBuild("nabl2.common"))
  bundleEmbedImplementation(compositeBuild("spoofax2.common"))
  bundleEmbedImplementation(compositeBuild("statix.common"))
  bundleEmbedImplementation(compositeBuild("stratego.common"))
  bundleEmbedImplementation(compositeBuild("stratego.pie"))

  bundleEmbedImplementation(compositeBuild("spoofax.compiler.interfaces"))

  bundleEmbedImplementation("org.metaborg:strategoxt-min-jar")
}

val privatePackage = listOf(
  "!mb.spoofax.lwb.eclipse", "!mb.spoofax.lwb.eclipse.*", // Our own packages should not be private.
  "mb.spoofax.lwb.*", // Embed `mb.spoofax.lwb`, `mb.spoofax.lwb.dynamicloading`, and co.
  "javax.inject.*", // Embed `javax.inject`
  "org.checkerframework.*", // Embed `org.checkerframework:checker-qual-android`
  "dagger.*", // Embed `com.google.dagger:dagger-compiler`
  "io.github.classgraph.*", "nonapi.io.github.classgraph.*", // Embed `io.github.classgraph:classgraph`.
  "mb.pie.task.archive.*", "mb.pie.task.java.*", // Embed PIE task modules.
  // Embed Spoofax 3 common/pie modules.
  "mb.constraint.common.*",
  "mb.constraint.pie.*",
  "mb.esv.common.*",
  "mb.jsglr.common.*",
  "mb.jsglr1.common.*",
  "mb.jsglr1.pie.*",
  "mb.nabl2.common.*",
  "mb.spoofax2.common.*",
  "mb.statix.common.*",
  "mb.stratego.common.*",
  "mb.stratego.pie.*",
  "mb.spoofax.compiler.interfaces.*", // Embed `spoofax.compiler.interfaces`
  "org.strategoxt.*", "org.spoofax.interpreter.*" // Embed Stratego and co
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
