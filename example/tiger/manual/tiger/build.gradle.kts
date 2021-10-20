plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.devenv.spoofax.gradle.base")
}

sourceSets {
  main {
    java {
      srcDir("$buildDir/generated/sources/spoofax/java")
    }
  }
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  api("org.metaborg:common")
  api(compositeBuild("spoofax.compiler.interfaces"))
  api(compositeBuild("spoofax.resource"))
  api(compositeBuild("jsglr1.common"))
  api(compositeBuild("esv.common"))
  api(compositeBuild("stratego.common"))
  api(compositeBuild("constraint.common"))

  implementation(compositeBuild("nabl2.common"))
  implementation("org.metaborg:strategoxt-min-jar")

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

fun copySpoofaxLanguageResources(
  dependency: Dependency,
  destinationPackage: String,
  includeStrategoClasses: Boolean,
  vararg resources: String
) {
  val allResources = resources.toMutableList()
  if(includeStrategoClasses) {
    allResources.add("target/metaborg/stratego.jar")
  }

  // Add language dependency.
  dependencies.add("compileLanguage", dependency)

  // Unpack the '.spoofax-language' archive.
  val languageFiles = project.configurations.getByName("languageFiles")
  val unpackSpoofaxLanguageDir = "$buildDir/unpackedSpoofaxLanguage/"
  val unpackSpoofaxLanguageTask = tasks.register<Sync>("unpackSpoofaxLanguage") {
    dependsOn(languageFiles)
    from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
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
  val copyMainTask = tasks.register<Copy>("copyMainResources") {
    dependsOn(unpackSpoofaxLanguageTask)
    into(sourceSets.main.get().java.outputDir)
    into(destinationPackage) { with(resourcesCopySpec) }
    if(includeStrategoClasses) {
      into(".") { with(strategoCopySpec) }
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
  }
  tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
}
copySpoofaxLanguageResources(
  dependencies.create(compositeBuild("tiger.spoofaxcore")),
  "mb/tiger",
  true,
  "target/metaborg/editor.esv.af", "target/metaborg/sdf.tbl"
)
