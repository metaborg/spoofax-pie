rootProject.name = "spoofax3.lwb.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax.root) will include these composite builds.
if(gradle.parent == null) {
  includeBuild("../core")
}

fun String.includeProject(id: String, path: String = "$this/$id") {
  include(id)
  project(":$id").projectDir = file(path)
}

"sdf3".run {
  includeProject("sdf3")
  includeProject("sdf3.spoofax")
  includeProject("sdf3.cli")
  includeProject("sdf3.eclipse.externaldeps")
  includeProject("sdf3.eclipse")
  includeProject("sdf3.intellij")
}

"stratego".run {
  includeProject("stratego")
  includeProject("stratego.spoofax")
  includeProject("stratego.cli")
  includeProject("stratego.eclipse.externaldeps")
  includeProject("stratego.eclipse")
  includeProject("stratego.intellij")
}

"esv".run {
  includeProject("esv")
  includeProject("esv.spoofax")
  includeProject("esv.cli")
  includeProject("esv.eclipse.externaldeps")
  includeProject("esv.eclipse")
  includeProject("esv.intellij")
}

"libspoofax2".run {
  includeProject("libspoofax2")
  includeProject("libspoofax2.spoofax")
}

include("spoofax.compiler.spoofax3")
include("spoofax.compiler.spoofax3.dagger")
include("spoofax.compiler.gradle.spoofax3")
