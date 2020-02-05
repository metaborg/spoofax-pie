plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.13"
  id("org.metaborg.gitonium") version "0.1.2"
}

tasks {
  register("buildCore") {
    group = "development"
    description = "Build all projects in the 'spoofax.core.root' composite build"
    val build = if(gradle.parent == null) {
      gradle.includedBuild("spoofax.core.root")
    } else {
      gradle.parent!!.includedBuild("spoofax.core.root")
    }
    dependsOn(build.task(":buildAll"))
  }
  register("buildExampleTiger") {
    group = "development"
    description = "Build all projects in the 'spoofax.example.tiger' composite build"
    val build = if(gradle.parent == null) {
      gradle.includedBuild("spoofax.core.root")
    } else {
      gradle.parent!!.includedBuild("spoofax.core.root")
    }
    dependsOn(build.task(":buildAll"))
  }
}
