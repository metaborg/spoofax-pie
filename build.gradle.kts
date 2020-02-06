plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.14"
  id("org.metaborg.gitonium") version "0.1.2"
}

tasks {
  register("buildCore") {
    group = "development"
    description = "Build all projects in the 'spoofax.core.root' composite build"
    dependsOn(includedBuildOrParent("spoofax.core.root").task(":buildAll"))
  }
  register("buildExampleTiger") {
    group = "development"
    description = "Build all projects in the 'spoofax.example.tiger' composite build"
    dependsOn(includedBuildOrParent("spoofax.example.tiger").task(":buildAll"))
  }
}

fun Project.includedBuildOrParent(name: String): IncludedBuild {
  return if(gradle.parent == null) {
    gradle.includedBuild(name)
  } else {
    gradle.parent!!.includedBuild(name)
  }
}
