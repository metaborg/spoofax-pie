import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  implementation("$group:spoofax2.common:$version")

  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

val packageId = "mb.statix.spoofax"
val taskPackageId = "$packageId.task"

languageAdapterProject {
  languageProject.set(project(":statix"))
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer().run {
      // Enable manual class implementation
      classKind(ClassKind.Extended)
      // Manual analyze multi implementation to add Spoofax2ProjectContext
      genAnalyzeMultiTaskDef(taskPackageId, "GeneratedStatixAnalyzeMulti")
      manualAnalyzeMultiTaskDef(taskPackageId, "StatixAnalyzeMulti")
    }
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  isMultiFile(true)
  addTaskDefs(taskPackageId, "StatixCompile")
}
