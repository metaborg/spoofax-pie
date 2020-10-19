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
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageAdapterProject {
  languageProject.set(project(":calc"))
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.calc.spoofax"
  val taskPackageId = "$packageId.task"
  val commandPackageId = "$packageId.command"
}
