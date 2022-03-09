import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {

}

languageProject {
  shared {
    name("Sdf3ExtStatix")
    defaultClassPrefix("Sdf3ExtStatix")
    defaultPackageId("mb.sdf3_ext_statix")
    fileExtensions(listOf()) // No file extensions.
  }
  compilerInput {
    withStrategoRuntime().run {
      baseStrategoRuntimeBuilderFactory("mb.sdf3_ext_statix.stratego", "BaseSdf3ExtStatixStrategoRuntimeBuilderFactory")
      extendStrategoRuntimeBuilderFactory("mb.sdf3_ext_statix.stratego", "Sdf3ExtStatixStrategoRuntimeBuilderFactory")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:sdf3.ext.statix:${ext["spoofax2DevenvVersion"]}"))
  }
}

languageAdapterProject {
  compilerInput {
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  compositionGroup("mb.spoofax.lwb")

  val packageId = "mb.sdf3_ext_statix"
  val taskPackageId = "$packageId.task"

  addTaskDefs(
    TypeInfo.of(taskPackageId, "Sdf3ExtStatixGenerateStatix"),
    TypeInfo.of(taskPackageId, "Sdf3ExtStatixGenerateStratego")
  )
}
