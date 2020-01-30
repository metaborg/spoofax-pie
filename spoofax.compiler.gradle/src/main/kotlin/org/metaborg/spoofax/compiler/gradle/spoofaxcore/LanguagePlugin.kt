package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.afterEvaluate {
      val extension = extensions.getByType<SpoofaxCompilerExtension>()
      extension.languageProject.set(project.providers.provider { project.toGradleProject() })
      afterEvaluate(this, extension)
    }
  }

  private fun afterEvaluate(project: Project, extension: SpoofaxCompilerExtension) {
    val compilerSettings = extension.finalized
    val input = compilerSettings.languageProjectInput
    val compiler = extension.languageProjectCompiler

    compiler.generateGradleFiles(input)
    project.apply(from = input.shared().languageProject().baseDirectory().relativizeToString(input.generatedGradleKtsFile()))

    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      inputs.property("input", input)
      outputs.files(input.generatedLanguageProjectFiles().map { extension.resourceService.toLocalFile(it) })
      doLast {
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
