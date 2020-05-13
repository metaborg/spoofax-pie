rootProject.name = "spoofax.example.sdf3"

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

include("sdf3")
include("sdf3.spoofax")
include("sdf3.cli")
include("sdf3.eclipse.externaldeps")
include("sdf3.eclipse")
include("sdf3.intellij")
