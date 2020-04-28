rootProject.name = "spoofax.example.tiger.generated"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax) will include these composite builds.
if(gradle.parent == null) {
  includeBuild("../../../core")
  includeBuild("../spoofaxcore")
}

include("tiger")
include("tiger.spoofax")
include("tiger.cli")
include("tiger.eclipse.externaldeps")
include("tiger.eclipse")
include("tiger.intellij")
