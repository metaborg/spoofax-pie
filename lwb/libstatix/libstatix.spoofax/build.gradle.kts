import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageAdapterProject {
  languageProject.set(project(":libstatix"))
  compilerInput {
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {

}
