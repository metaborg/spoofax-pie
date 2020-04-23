@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*

open class CliProjectCompilerSettings(
  val rootGradleProject: Project,
  val adapterGradleProject: Project,
  val compiler: CliProjectCompiler.Input.Builder = CliProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): CliProjectCompilerFinalized {
    val project = gradleProject.toSpoofaxCompilerProject()
    val spoofaxCompilerExtension: SpoofaxCompilerExtension = rootGradleProject.extensions.getByType()
    val shared = spoofaxCompilerExtension.shared
    val adapterProjectCompilerExtension: AdapterProjectCompilerExtension = adapterGradleProject.extensions.getByType()
    val adapterProjectCompilerInput = adapterProjectCompilerExtension.finalized.input

    val input = this.compiler.shared(shared).project(project).adapterProjectCompilerInput(adapterProjectCompilerInput).build()

    val resourceService = spoofaxCompilerExtension.resourceService
    val cliProjectCompiler = spoofaxCompilerExtension.cliProjectCompiler
    return CliProjectCompilerFinalized(resourceService, cliProjectCompiler, input)
  }
}

open class CliProjectCompilerExtension(project: Project) {
  val settings: Property<CliProjectCompilerSettings> = project.objects.property()

  companion object {
    internal const val id = "cliProjectCompiler"
  }

  internal val finalizedProvider: Provider<CliProjectCompilerFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<CliProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: CliProjectCompilerFinalized by lazy {
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("CLI project compiler settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class CliProjectCompilerFinalized(
  val resourceService: ResourceService,
  val compiler: CliProjectCompiler,
  val input: CliProjectCompiler.Input
)

@Suppress("unused")
open class CliPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = CliProjectCompilerExtension(project)
    project.extensions.add(CliProjectCompilerExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-application")

    configureCliLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
  }

  private fun configureCliLanguageProjectTask(project: Project, extension: CliProjectCompilerExtension) {
    val configureCliProjectTask = project.tasks.register("configureCliProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
        finalized.compiler.getDependencies(input).forEach {
          it.addToDependencies(project)
        }
        project.configure<JavaApplication> {
          mainClassName = input.main().qualifiedId()
        }
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureCliProjectTask)
  }

  private fun configureCompileTask(project: Project, extension: CliProjectCompilerExtension) {
    val compileTask = project.tasks.register("spoofaxCompileCliProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)
      outputs.files(extension.resourceServiceProvider.flatMap { resourceService -> extension.inputProvider.map { input -> input.providedFiles().map { resourceService.toLocalFile(it) } } })

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.deleteGenSourceSpoofaxDirectory(input.project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
