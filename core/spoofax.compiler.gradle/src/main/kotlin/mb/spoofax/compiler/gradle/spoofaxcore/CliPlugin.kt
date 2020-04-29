@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.spoofaxcore.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class CliProjectSettings(
  val builder: CliProjectCompiler.Input.Builder = CliProjectCompiler.Input.builder()
) {
  internal fun finalize(project: Project, adapterProject: Project): CliProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = this.builder
      .shared(languageProjectFinalized.shared)
      .project(project.toSpoofaxCompilerProject())
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return CliProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class CliProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val settings: Property<CliProjectSettings> = project.objects.property()

  init {
    settings.convention(CliProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxCliProject"
    private const val name = "Spoofax language CLI project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }

  internal val finalized: CliProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, adapterProjectFinalized)
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

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: CliProjectFinalized) {
    configureProject(project, finalized)
    configureCompileTask(project, finalized)
  }

  private fun configureProject(project: Project, finalized: CliProjectFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.configure<JavaApplication> {
      mainClassName = input.main().qualifiedId()
    }
  }

  private fun configureCompileTask(project: Project, finalized: CliProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileCliProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
