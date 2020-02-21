rootProject.name = "spoofax.core.root"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central, JCenter).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    jcenter()
    // Required by spoofax.gradle plugin.
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("https://nexus.usethesource.io/content/repositories/public/")
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

include("spoofax.depconstraints")

include("common")

include("completions.common")
include("jsglr.common")
include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("constraint.common")
include("nabl2.common")
include("statix.common")
include("spoofax2.common")

include("spoofax.core")
include("spoofax.cli")
include("spoofax.intellij")
include("spoofax.eclipse")
include("spoofax.eclipse.externaldeps")
include("spoofax.compiler")
include("spoofax.compiler.interfaces")
include("spoofax.compiler.gradle")
