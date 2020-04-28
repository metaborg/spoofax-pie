plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.19"
  id("org.metaborg.gitonium") version "0.1.2"
}

tasks {
  register("buildCore") {
    group = "development"
    description = "Build all projects in the 'spoofax.core.root' composite build"
    tryDependOnTaskInIncludedBuild("spoofax.core.root", ":buildAll")
  }

  register("buildExampleTigerSpoofaxcore") {
    group = "development"
    description = "Build all projects in the 'spoofax.example.tiger.spoofaxcore' composite build"
    tryDependOnTaskInIncludedBuild("spoofax.example.tiger.spoofaxcore", ":buildAll")
  }
  register("buildExampleTigerGenerated") {
    group = "development"
    description = "Build all projects in the 'spoofax.example.tiger.generated' composite build"
    tryDependOnTaskInIncludedBuild("spoofax.example.tiger.generated", ":buildAll")
  }
  register("buildExampleTigerManual") {
    group = "development"
    description = "Build all projects in the 'spoofax.example.tiger.manual' composite build"
    tryDependOnTaskInIncludedBuild("spoofax.example.tiger.manual", ":buildAll")
  }
}

fun Task.tryDependOnTaskInIncludedBuild(includedBuildName: String, taskPath: String) {
  try {
    dependsOn(gradle.includedBuild(includedBuildName).task(taskPath))
  } catch(e: UnknownDomainObjectException) {
    logger.warn("Could not depend on task '$taskPath' in included build with name '$includedBuildName'")
  }
}
