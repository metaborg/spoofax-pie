plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))
  api(project(":jsglr1.common"))
  api(project(":esv.common"))

  compileOnly("org.checkerframework:checker-qual-android")
}

// Copy files from org.metaborg.lang.tiger into src/main/resources.
repositories {
  // Required repositories for Tiger dependency.
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
  isTransitive = false // Don't care about transitive dependencies, just want the Tiger spoofax-language artifact.
}
tigerResources.dependencies.add(tigerDependency)
// First unpack Tiger language resources, because we cannot copy from a subdirectory in a ziptree.
val unpackResourcesTask = tasks.register<Sync>("unpackResources") {
  dependsOn(tigerResources)
  from({ tigerResources.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
  into("$buildDir/unpacked")
  include("target/metaborg/editor.esv.af", "target/metaborg/sdf.tbl")
}
// Copy resources into 'src/main/resources/mb/tiger', so the resources finally end up in the 'mb.tiger' package in the resulting JAR.
val copyTigerResourcesTask = tasks.register<Sync>("copyTigerResources") {
  dependsOn(unpackResourcesTask)
  from("$buildDir/unpacked/target/metaborg")
  into("src/main/resources/mb/tiger")
  include("editor.esv.af", "sdf.tbl")
}
tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME).dependsOn(copyTigerResourcesTask)

// Copy resources into classes directory, to make them accessible as class resources at runtime.
val mainSourceSet: SourceSet = sourceSets.main.get()
val copyResourcesTask = tasks.create<Copy>("copyResources") {
  from(mainSourceSet.resources.sourceDirectories)
  into(mainSourceSet.java.outputDir)
}
tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME).dependsOn(copyResourcesTask)
// Same for test resources.
val testSourceSet: SourceSet = sourceSets.test.get()
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
  from(testSourceSet.resources.sourceDirectories)
  into(testSourceSet.java.outputDir)
}
tasks.getByName(JavaPlugin.PROCESS_TEST_RESOURCES_TASK_NAME).dependsOn(copyTestResourcesTask)
