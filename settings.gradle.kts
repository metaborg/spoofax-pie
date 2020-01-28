rootProject.name = "spoofax.pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central, JCenter).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    jcenter()
    // Required by spoofax.gradle plugin.
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

include("spoofax.depconstraints")

include("common")

include("jsglr.common")
include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("constraint.common")
include("nabl2.common")
include("statix.common")

include("spoofax.core")
include("spoofax.cli")
include("spoofax.intellij")
include("spoofax.eclipse")
include("spoofax.eclipse.externaldeps")
include("spoofax.compiler")
include("spoofax.compiler.interfaces")
include("spoofax.compiler.gradle")

// Reusable function for configuring included builds with a dependency substitution that attempts to use local projects.
// Note that is only works if the name of the module (artifactId) exactly matches the name of the project.
// TODO: move this to gradle.config plugin?
val useLocalProjectsSubstitution: ConfigurableIncludedBuild.() -> Unit = {
  dependencySubstitution {
    all {
      if(requested is ModuleComponentSelector) {
        // Explicit cast as smart cast is impossible due to open getter method.
        val selector = requested as ModuleComponentSelector
        val project = findProject(selector.module)
        if(project != null) {
          useTarget(project(":${selector.module}"), "Substituting with local project")
        }
      }
    }
  }
}
// Include rest of Tiger projects as a composite build (`spoofax.example.tiger`), because it requires the
// `spoofax.compiler.gradle` Gradle plugin from this root multi-project build (`spoofax.pie`), and Gradle plugin are
// only available in the same build as a composite build.
includeBuild("example/tiger", useLocalProjectsSubstitution)
// Include `org.metaborg.lang.tiger` as a separate composite build, because `spoofax.compiler`'s tests depend on it.
// Including it in `example/tiger` would cause a cyclic composite build from `spoofax.pie` -> `spoofax.example.tiger` ->
// `spoofax.pie`. There is not really a cycle between projects, but there is between the composite builds, which
// Gradle does not allow.
includeBuild("example/tiger/org.metaborg.lang.tiger", useLocalProjectsSubstitution)
