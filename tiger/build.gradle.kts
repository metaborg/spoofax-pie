plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(project(":common"))
  api(project(":jsglr1.common"))
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}

// Copy files from org.metaborg.lang.tiger into src/main/resources.
repositories {
  maven("https://pluto-build.github.io/mvnrepository/")
  maven("https://sugar-lang.github.io/mvnrepository/")
  maven("http://nexus.usethesource.io/content/repositories/public/")
}
val tigerDependency = dependencies.project(":org.metaborg.lang.tiger", Dependency.DEFAULT_CONFIGURATION)
tigerDependency.artifact {
  name = tigerDependency.name
  type = "spoofax-language"
  extension = "spoofax-language"
}
val tigerResources = configurations.create("tigerResources") {
  isTransitive = false
}
tigerResources.dependencies.add(tigerDependency)
val copyTigerResourcesTask = tasks.register<Sync>("copyTigerResources") {
  dependsOn(tigerResources)
  from({ tigerResources.map { zipTree(it) } }) // Closure inside `from` to defer evaluation until task execution time.
  into("src/main/resources")
  include("target/metaborg/editor.esv.af", "target/metaborg/sdf.tbl")
}
tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME).dependsOn(copyTigerResourcesTask)
