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

"metalang/cfg".run {
  includeProject("cfg")
  includeProject("cfg.cli")
  includeProject("cfg.eclipse")
  includeProject("cfg.intellij")
  includeProject("cfg.spoofax2")
}
"metalang/sdf3".run {
  includeProject("sdf3")
  includeProject("sdf3.cli")
  includeProject("sdf3.eclipse")
  includeProject("sdf3.intellij")
}
"metalang/stratego".run {
  includeProject("stratego")
  includeProject("stratego.cli")
  includeProject("stratego.eclipse")
  includeProject("stratego.intellij")
}
"metalang/esv".run {
  includeProject("esv")
  includeProject("esv.cli")
  includeProject("esv.eclipse")
  includeProject("esv.intellij")
}
"metalang/statix".run {
  includeProject("statix")
  includeProject("statix.cli")
  includeProject("statix.eclipse")
  includeProject("statix.intellij")
}
"metalib/libspoofax2".run {
  includeProject("libspoofax2")
  includeProject("libspoofax2.eclipse")
}
"metalib/libstatix".run {
  includeProject("libstatix")
  includeProject("libstatix.eclipse")
}

include("spoofax.lwb.compiler")
include("spoofax.lwb.compiler.cfg")
include("spoofax.lwb.compiler.dagger")
include("spoofax.lwb.compiler.gradle")

include("spoofax.lwb.dynamicloading")

include("spoofax.lwb.eclipse")
