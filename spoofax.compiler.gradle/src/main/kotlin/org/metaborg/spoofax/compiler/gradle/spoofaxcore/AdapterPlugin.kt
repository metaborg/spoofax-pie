package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.getByType<SpoofaxCompilerExtension>()
    extension.adapterProject.set(project.providers.provider { project.toGradleProject() })
    project.afterEvaluate {
      afterEvaluate(this, extension)
    }

  }

  private fun afterEvaluate(project: Project, extension: SpoofaxCompilerExtension) {
    val compilerSettings = extension.finalized
    val input = compilerSettings.adapterProjectInput!!
    val compiler = extension.adapterProjectCompiler

    compiler.generateGradleFiles(input)
    project.apply(from = input.relativeGeneratedGradleKtsFile())

    val compileTask = project.tasks.register("spoofaxCompileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { extension.resourceService.toLocalFile(it) })
      doLast {
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
