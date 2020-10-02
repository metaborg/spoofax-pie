rootProject.name = "spoofax3.example.root"

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
  includeBuild("../lwb")
}

fun String.includeProject(id: String, path: String = "$this/$id") {
  include(id)
  project(":$id").projectDir = file(path)
}

"tiger/spoofaxcore".run {
  includeProject("tiger.spoofaxcore")
}
"tiger/manual".run {
  includeProject("tiger", "tiger/manual/tiger")
  includeProject("tiger.spoofax", "tiger/manual/tiger.spoofax")
  includeProject("tiger.cli", "tiger/manual/tiger.cli")
  includeProject("tiger.eclipse.externaldeps", "tiger/manual/tiger.eclipse.externaldeps")
  includeProject("tiger.eclipse", "tiger/manual/tiger.eclipse")
  includeProject("tiger.intellij", "tiger/manual/tiger.intellij")
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

"calc".run {
  includeProject("calc")
  includeProject("calc.spoofax")
}

"multilang/spoofaxcore".run {
  includeProject("signature-interface.spoofaxcore")
  includeProject("module-interface.spoofaxcore")
  includeProject("minisdf.spoofaxcore")
  includeProject("ministr.spoofaxcore")
}

"multilang/generated".run {
  includeProject("signature")
  includeProject("module")

  includeProject("minisdf")
  includeProject("minisdf.spoofax")
  includeProject("minisdf.eclipse.externaldeps")
  includeProject("minisdf.eclipse")
  includeProject("minisdf.cli")

  includeProject("ministr")
  includeProject("ministr.spoofax")
  includeProject("ministr.eclipse.externaldeps")
  includeProject("ministr.eclipse")
  includeProject("ministr.cli")

  includeProject("multilang.eclipse")
  includeProject("multilang.test")
}


