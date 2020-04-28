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

open class CliProjectSettings(
  val adapterGradleProject: Project,
  val builder: CliProjectCompiler.Input.Builder = CliProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): CliProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterGradleProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = this.builder
      .shared(languageProjectFinalized.shared)
      .project(gradleProject.toSpoofaxCompilerProject())
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return CliProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class CliProjectExtension(project: Project) {
  val settings: Property<CliProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "spoofaxCliProject"
  }

  internal val finalizedProvider: Provider<CliProjectFinalized> = project.providers.provider { finalized }
  internal val inputProvider: Provider<CliProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: CliProjectFinalized by lazy {
    project.logger.lifecycle("Finalizing Spoofax language CLI project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Spoofax language CLI project settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class CliProjectFinalized(
  val input: CliProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.cliProjectCompiler
}

@Suppress("unused")
open class CliPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = CliProjectExtension(project)
    project.extensions.add(CliProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-application")

    configureCliLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
  }

  private fun configureCliLanguageProjectTask(project: Project, extension: CliProjectExtension) {
    val configureTask = project.tasks.register("spoofaxConfigureCliProject") {
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
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureTask)
  }

  private fun configureCompileTask(project: Project, extension: CliProjectExtension) {
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
