rootProject.name = "spoofax3.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Only include composite builds when this is the root project (it has no parent). Otherwise, the parent project
// (devenv) will include these composite builds, as IntelliJ does not support nested composite builds.
if(gradle.parent == null) {
  // We split the build up into one main composite build in the 'core' directory, because it builds Gradle plugins,
  // which we want to test. Gradle plugins are not directly available in a multi-project build, therefore a separate
  // composite build is required. Included builds listed below can use the Gradle plugins built in 'core'.
  includeBuild("core")
  // The 'lwb' (language workbench) composite build includes the meta-languages which in turn depend on Gradle plugins
  // from the 'core' composite build. Additionally, 'lwb' also contains Gradle plugins for building languages.
  includeBuild("lwb")
  // The 'example' composite build has example languages, some based on Gradle plugins from 'core', and some based on
  // Gradle plugins from 'lwb'.
  includeBuild("example")
}
