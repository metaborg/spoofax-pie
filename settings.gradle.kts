rootProject.name = "spoofax.pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

// common - common low-level utility (e.g., messages) [try to copy from spoofax.api of prototype1]

// jsglr2 (external) - parsing with JSGLR2
// esv - syntax-based styling with ESV [try to copy from prototype1]

// spoofax - interfaces, utility, pipelines, etc. for gluing.
// spoofax.cmd - adapting spoofax to command-line.
// spoofax.eclipse - adapting spoofax to Eclipse plugins.

// lang - raw implementation of language's several parts: parser and syntax-based styler.
// lang.spoofax - binding to spoofax interfaces.
// lang.cmd - glue lang.spoofax to spoofax.cmd, and provide and executable JAR.
// lang.eclipse - glue lang.spoofax to spoofax.eclipse, and provide an Eclipse plugin.