rootProject.name = "spoofax.root"

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

// Only include composite builds when this is the root project (it has no parent). Otherwise, the root project (devenv)
// will include these composite builds, as IntelliJ does not support nested composite builds.
if(gradle.parent == null) {
  // We split the build up into one main composite build, which is defined in the 'compositebuild' directory, because
  // that build builds Gradle plugins, which we want to test. Gradle plugins are not directly available in a
  // multi-project build, therefore a separate composite build is required. Included builds listed below can use the
  // Gradle plugins built in this composite build.
  includeBuild("compositebuild")
  includeBuild("example/tiger")
}

// The main build is configured in 'compositebuild/settings.gradle.kts'.
