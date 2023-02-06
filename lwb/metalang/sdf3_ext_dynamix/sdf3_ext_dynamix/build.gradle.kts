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
    name("Sdf3ExtDynamix")
    defaultClassPrefix("Sdf3ExtDynamix")
    defaultPackageId("mb.sdf3_ext_dynamix")
    fileExtensions(listOf()) // No file extensions.
  }
  compilerInput {
    withStrategoRuntime()
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.project(":sdf3_ext_dynamix.spoofax2"))
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

  val packageId = "mb.sdf3_ext_dynamix"
  val taskPackageId = "$packageId.task"

  addTaskDefs(
    TypeInfo.of(taskPackageId, "Sdf3ExtDynamixGenerateDynamix")
  )
}
