rootProject.name = "spoofax.pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central, JCenter).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    jcenter()
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

include("depconstraints")

include("common")

include("jsglr.common")
include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("constraint.common")
include("nabl2.common")
include("statix.common")

include("spoofax.core")
include("spoofax.cmd")
include("spoofax.intellij")
include("spoofax.eclipse")
include("spoofax.eclipse.externaldeps")

include("tiger")
include("tiger.spoofax")
include("tiger.cmd")
include("tiger.intellij")
include("tiger.eclipse")
include("tiger.eclipse.externaldeps")

include("org.metaborg.lang.tiger")
include("lang.stlcrec")
