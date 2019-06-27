plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))
  api(project(":jsglr1.common"))
  api(project(":esv.common"))
  api(project(":stratego.common"))
  api(project(":constraint.common"))

  implementation(project(":nabl2.common"))
  implementation(project(":statix.common"))
  implementation("org.metaborg:strategoxt-min-jar")

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation("org.metaborg:log.backend.noop")

  testCompileOnly("org.checkerframework:checker-qual-android")
}

fun copySpoofaxLanguageResources(
  projectPath: String,
  destinationPackage: String,
  includeStrategoClasses: Boolean,
  includeStrategoJavastratClasses: Boolean,
  vararg resources: String
) {
  val allResources = resources.toMutableList()
  if(includeStrategoClasses) {
    allResources.add("target/metaborg/stratego.jar")
  }
  if(includeStrategoJavastratClasses) {
    allResources.add("target/metaborg/stratego-javastrat.jar")
  }

  // Create dependency to the '.spoofax-language' artifact.
  val dependency = run {
    val dep = dependencies.project(projectPath, Dependency.DEFAULT_CONFIGURATION)
    dep.isTransitive = false // Don't care about transitive dependencies, just want the '.spoofax-language' artifact.
    dep.artifact {
      name = dep.name
      type = "spoofax-language"
      extension = "spoofax-language"
    }
    dep
  }
  // Create 'spoofaxLanguage' configuration that contains the dependency.
  val configuration = configurations.create("spoofaxLanguage") {
    dependencies.add(dependency)
  }
  // Unpack the '.spoofax-language' archive.
  val unpackSpoofaxLanguageDir = "$buildDir/unpackedSpoofaxLanguage/"
  val unpackSpoofaxLanguageTask = tasks.register<Sync>("unpackSpoofaxLanguage") {
    dependsOn(configuration)
    from({ configuration.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
    into(unpackSpoofaxLanguageDir)
    include(allResources)
  }
  // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
  val resourcesCopySpec = copySpec {
    from(unpackSpoofaxLanguageDir)
    include(*resources)
  }
  val strategoCopySpec = copySpec {
    from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
    exclude("META-INF")
  }
  val strategoJavastratCopySpec = copySpec {
    from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego-javastrat.jar"))
    exclude("META-INF")
  }
  val copyMainTask = tasks.register<Copy>("copyMainResources") {
    dependsOn(unpackSpoofaxLanguageTask)
    into(sourceSets.main.get().java.outputDir)
    into(destinationPackage) { with(resourcesCopySpec) }
    if(includeStrategoClasses) {
      into(".") { with(strategoCopySpec) }
    }
    if(includeStrategoJavastratClasses) {
      into(".") { with(strategoJavastratCopySpec) }
    }
  }
  tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
  val copyTestTask = tasks.register<Copy>("copyTestResources") {
    dependsOn(unpackSpoofaxLanguageTask)
    into(sourceSets.test.get().java.outputDir)
    into(destinationPackage) { with(resourcesCopySpec) }
    if(includeStrategoClasses) {
      into(".") { with(strategoCopySpec) }
    }
    if(includeStrategoJavastratClasses) {
      into(".") { with(strategoJavastratCopySpec) }
    }
  }
  tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
}
copySpoofaxLanguageResources(
  ":lang.stlcrec",
  "mb/stlcrec",
  false,
  false,
  "target/metaborg/editor.esv.af", "target/metaborg/sdf.tbl", "target/metaborg/stratego.ctree", "src-gen/statix/statics.spec.aterm"
)
