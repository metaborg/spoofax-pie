rootProject.name = "spoofax"

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
    maven("http://nexus.usethesource.io/content/repositories/public/")
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

fun includeProject(id: String) {
  include(id)
  project(":$id").projectDir = file("../$id")
}

includeProject("spoofax.depconstraints")

includeProject("common")

includeProject("jsglr.common")
includeProject("jsglr1.common")
includeProject("jsglr2.common")
includeProject("esv.common")
includeProject("stratego.common")
includeProject("constraint.common")
includeProject("nabl2.common")
includeProject("statix.common")

includeProject("spoofax.core")
includeProject("spoofax.cli")
includeProject("spoofax.intellij")
includeProject("spoofax.eclipse")
includeProject("spoofax.eclipse.externaldeps")
includeProject("spoofax.compiler")
includeProject("spoofax.compiler.interfaces")
includeProject("spoofax.compiler.gradle")
