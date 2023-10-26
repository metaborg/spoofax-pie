import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}

languageProject {
  shared {
    name("DynamixRuntime")
    defaultClassPrefix("DynamixRuntime")
    defaultPackageId("mb.dynamix_runtime")
    fileExtensions(listOf()) // No file extensions.
  }
  compilerInput {
    withStrategoRuntime().run {
      addNaBL2Primitives(true) // required for attribute lookup
      addStatixPrimitives(true) // required for attribute lookup
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.project(":dynamix_runtime.spoofax2"))
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

  val packageId = "mb.dynamix_runtime"
  val taskPackageId = "$packageId.task"

  addTaskDefs(
    TypeInfo.of(taskPackageId, "DynamixRuntimeRunSpecification"),
    TypeInfo.of(taskPackageId, "DynamixRuntimePrettyPrint")
  )
}
