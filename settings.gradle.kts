rootProject.name = "spoofax.pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
    // Required by Gradle plugins.
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central (backup).
    jcenter() // JCenter.
  }
}

include("depconstraints")

include("common")

include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("constraint.common")

include("spoofax.core")
include("spoofax.cmd")
include("spoofax.eclipse")
include("spoofax.eclipse.externaldeps")

include("tiger")
include("tiger.spoofax")
include("tiger.cmd")
include("tiger.eclipse")
include("tiger.eclipse.externaldeps")
include("org.metaborg.lang.tiger")
