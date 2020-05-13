rootProject.name = "spoofax.example.mod"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

enableFeaturePreview("GRADLE_METADATA")

// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax) will include these composite builds.
if(gradle.parent == null) {
  includeBuild("../../core")
}

include("mod.spoofaxcore")
include("mod")
include("mod.spoofax")
include("mod.cli")
include("mod.eclipse.externaldeps")
include("mod.eclipse")
include("mod.intellij")
