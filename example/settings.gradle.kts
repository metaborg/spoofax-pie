rootProject.name = "spoofax.example"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax) will include these composite builds.
if(gradle.parent == null) {
  includeBuild("../core")
}

fun String.includeProject(id: String, path: String = "$this/$id") {
  include(id)
  project(":$id").projectDir = file(path)
}

"tiger/spoofaxcore".run {
  includeProject("tiger.spoofaxcore")
}
"tiger/generated".run {
  includeProject("tiger")
  includeProject("tiger.spoofax")
  includeProject("tiger.cli")
  includeProject("tiger.eclipse.externaldeps")
  includeProject("tiger.eclipse")
  includeProject("tiger.intellij")
}
"tiger/manual".run {
  // TODO: rename the Tiger manual project directories to include .manual in their name.
  includeProject("tiger.manual", "tiger/manual/tiger")
  includeProject("tiger.manual.spoofax", "tiger/manual/tiger.spoofax")
  includeProject("tiger.manual.cli", "tiger/manual/tiger.cli")
  includeProject("tiger.manual.eclipse.externaldeps", "tiger/manual/tiger.eclipse.externaldeps")
  includeProject("tiger.manual.eclipse", "tiger/manual/tiger.eclipse")
  includeProject("tiger.manual.intellij", "tiger/manual/tiger.intellij")
}

"mod".run {
  includeProject("mod.spoofaxcore")
  includeProject("mod")
  includeProject("mod.spoofax")
  includeProject("mod.cli")
  includeProject("mod.eclipse.externaldeps")
  includeProject("mod.eclipse")
  includeProject("mod.intellij")
}

"sdf3".run {
  includeProject("sdf3")
  includeProject("sdf3.spoofax")
  includeProject("sdf3.cli")
  includeProject("sdf3.eclipse.externaldeps")
  includeProject("sdf3.eclipse")
  includeProject("sdf3.intellij")
}
