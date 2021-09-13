import mb.spoofax.compiler.adapter.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageAdapterProject {
  languageProject.set(project(":mod"))
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    withConstraintAnalyzer()
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {

}
