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

fun includeProject(path: String, id: String = "spoofax.${path.replace('/', '.')}") {
  include(id)
  project(":$id").projectDir = file(path)
}

include("common") // common low-level utility (e.g., messages) [try to copy from spoofax.api of prototype1]

include("jsglr2.pie") // parsing with JSGLR2 + pie functions (this should be moved to JSGLR2 in the future)
include("esv.pie") // syntax-based styling with ESV [try to copy from prototype1] + pie functions (this should be moved to ESV in the future)

include("spoofax.core") // interfaces, utility, pipelines, etc. for gluing.
include("spoofax.cmd") // adapting spoofax to command-line.
include("spoofax.eclipse") // adapting spoofax to Eclipse plugins.

include("tiger") // raw implementation of tigers's several parts: parser and syntax-based styler.
include("tiger.spoofax") // binding to spoofax interfaces.
include("tiger.cmd") // glue tiger.spoofax to spoofax.cmd, and provide and executable JAR.
include("tiger.eclipse") // glue Tiger.spoofax to spoofax.eclipse, and provide an Eclipse plugin.
include("org.metaborg.lang.tiger") // tiger in Spoofax Core, used to copy built artifacts.
