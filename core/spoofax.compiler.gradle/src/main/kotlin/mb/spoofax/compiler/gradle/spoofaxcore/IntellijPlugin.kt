@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.IntelliJPluginExtension

open class IntellijProjectCompilerSettings(
  val compiler: IntellijProjectCompiler.Input.Builder = IntellijProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, project: GradleProject, adapterProjectCompilerInput: AdapterProjectCompiler.Input): IntellijProjectCompiler.Input {
    return compiler.shared(shared).project(project).adapterProjectCompilerInput(adapterProjectCompilerInput).build()
  }
}

open class IntellijProjectCompilerExtension(
  objects: ObjectFactory,
  project: Project,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<IntellijProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "intellijProjectCompiler"
  }

  init {
    settings.convention(IntellijProjectCompilerSettings())
  }

  internal val input: IntellijProjectCompiler.Input by lazy {
    settings.finalizeValue()
    settings.get().createInput(compilerExtension.shared, project.toSpoofaxCompilerProject(), compilerExtension.adapterProjectCompilerExtension.input)
  }
}

open class IntellijPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = IntellijProjectCompilerExtension(project.objects, project, compilerExtension)
    project.extensions.add(IntellijProjectCompilerExtension.id, extension)
    project.gradle.projectsEvaluated {
      afterEvaluate(project, compilerExtension, extension)
    }
  }

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: IntellijProjectCompilerExtension) {
    val compiler = compilerExtension.intellijProjectCompiler
    val resourceService = compilerExtension.resourceService
    val input = extension.input
    val compilerProject = input.project()
    project.configureGroup(compilerProject)
    project.configureVersion(compilerProject)
    project.configureGeneratedSources(compilerProject, resourceService)
    compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    configureCompilerTask(project, input, compiler, resourceService)
    configureIntellij(project, input)
  }

  private fun configureCompilerTask(
    project: Project,
    input: IntellijProjectCompiler.Input,
    compiler: IntellijProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileIntellijProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureIntellij(
    project: Project,
    input: IntellijProjectCompiler.Input
  ) {
    project.configure<IntelliJPluginExtension> {
      version = input.ideaVersion()
    }
  }
}
