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

  bundleImplementation(compositeBuild("spoofax.eclipse"))

  bundleImplementation(project(":sdf3.eclipse")) // HACK: put SDF3 first for testing SDF3 config injection.
  bundleImplementation(project(":cfg.eclipse"))
  bundleImplementation(project(":esv.eclipse"))
  bundleImplementation(project(":stratego.eclipse"))
  bundleImplementation(project(":statix.eclipse"))

  bundleImplementation(project(":libspoofax2.eclipse"))
  bundleImplementation(project(":libstatix.eclipse"))

  bundleEmbedImplementation(project(":spoofax.lwb.dynamicloading")) {
    exclude("org.metaborg", "cfg")
    exclude("org.metaborg", "esv")
    exclude("org.metaborg", "sdf3")
    exclude("org.metaborg", "stratego")
    exclude("org.metaborg", "statix")

    exclude("org.metaborg", "libspoofax2")
    exclude("org.metaborg", "libstatix")

    exclude("org.metaborg", "resource")
    exclude("org.metaborg", "common")
    exclude("org.metaborg", "pie.api")
    exclude("org.metaborg", "spoofax.core")
  }
  bundleEmbedImplementation("org.metaborg:pie.task.archive")
  bundleEmbedImplementation("org.metaborg:pie.task.java")

  bundleTargetPlatformApi(eclipse("javax.inject"))
}

val privatePackage = listOf(
  "!mb.spoofax.lwb.eclipse",
  "!mb.spoofax.lwb.eclipse.*",
  "mb.spoofax.lwb.*",
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
