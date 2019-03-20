plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}

// Copy files from org.metaborg.lang.tiger into src/main/resources.
repositories {
  maven("https://pluto-build.github.io/mvnrepository/")
  maven("https://sugar-lang.github.io/mvnrepository/")
  maven("http://nexus.usethesource.io/content/repositories/public/")
}
val tigerResources = configurations.create("tigerResources")
dependencies {
  tigerResources(project(":org.metaborg.lang.tiger", Dependency.DEFAULT_CONFIGURATION)) {
    artifact {
      name = this@tigerResources.name
      type = "spoofax-language"
      extension = "spoofax-language"
    }
  }
}
val copyTigerResourcesTask = tasks.register<Sync>("copyTigerResources") {
  dependsOn(tigerResources)
  from({ // Closure inside to defer evaluation until task execution time.
    tigerResources.map { zipTree(it) }
  }) // TODO: only copy relevant resources, and not transitive ones.
  into("src/main/resources")
}
tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME).dependsOn(copyTigerResourcesTask)

