@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.spoofaxcore.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.IntelliJPlugin

open class IntellijProjectSettings(
  val builder: IntellijProjectCompiler.Input.Builder = IntellijProjectCompiler.Input.builder()
) {
  internal fun finalize(project: Project, adapterProject: Project): IntellijProjectCompilerFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = builder
      .shared(languageProjectFinalized.shared)
      .project(project.toSpoofaxCompilerProject())
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return IntellijProjectCompilerFinalized(input, languageProjectFinalized.compilers)
  }
}

open class IntellijProjectCompilerExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val settings: Property<IntellijProjectSettings> = project.objects.property()

  init {
    settings.convention(IntellijProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxIntellijProject"
    private const val name = "Spoofax language IntelliJ project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }

  internal val finalized: IntellijProjectCompilerFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, adapterProjectFinalized)
  }
}

internal class IntellijProjectCompilerFinalized(
  val input: IntellijProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.intellijProjectCompiler
}

open class IntellijPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = IntellijProjectCompilerExtension(project)
    project.extensions.add(IntellijProjectCompilerExtension.id, extension)

    project.pluginManager.apply("org.metaborg.gradle.config.java-library")
    project.pluginManager.apply("org.jetbrains.intellij")

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: IntellijProjectCompilerFinalized) {
    configureProject(project, finalized)
    configureCompilerTask(project, finalized)
  }

  private fun configureProject(project: Project, finalized: IntellijProjectCompilerFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
      exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
    })
  }

  private fun configureCompilerTask(project: Project, finalized: IntellijProjectCompilerFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileIntellijProject") {
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

    // Make all of IntelliJ's tasks depend on our task, because we generate Java code and a plugin.xml file.
    project.tasks.getByName(IntelliJPlugin.BUILD_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PATCH_PLUGIN_XML_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PREPARE_SANDBOX_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.RUN_IDE_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PUBLISH_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.VERIFY_PLUGIN_TASK_NAME).dependsOn(compileTask)
  }
}
