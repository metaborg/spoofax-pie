@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.IntelliJPlugin

open class IntellijProjectCompilerSettings(
  val rootGradleProject: Project,
  val adapterGradleProject: Project,
  val compiler: IntellijProjectCompiler.Input.Builder = IntellijProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): IntellijProjectCompilerFinalized {
    val project = gradleProject.toSpoofaxCompilerProject()
    val rootProjectExtension: RootProjectExtension = rootGradleProject.extensions.getByType()
    val shared = rootProjectExtension.shared
    val adapterProjectExtension: AdapterProjectExtension = adapterGradleProject.extensions.getByType()
    val adapterProjectCompilerInput = adapterProjectExtension.finalized.input

    val input = compiler.shared(shared).project(project).adapterProjectCompilerInput(adapterProjectCompilerInput).build()

    val resourceService = rootProjectExtension.resourceService
    val intellijProjectCompiler = rootProjectExtension.intellijProjectCompiler
    return IntellijProjectCompilerFinalized(resourceService, intellijProjectCompiler, input)
  }
}

open class IntellijProjectCompilerExtension(project: Project) {
  val settings: Property<IntellijProjectCompilerSettings> = project.objects.property()

  companion object {
    internal const val id = "intellijProjectCompiler"
  }

  internal val finalizedProvider: Provider<IntellijProjectCompilerFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<IntellijProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: IntellijProjectCompilerFinalized by lazy {
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("IntelliJ project compiler settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class IntellijProjectCompilerFinalized(
  val resourceService: ResourceService,
  val compiler: IntellijProjectCompiler,
  val input: IntellijProjectCompiler.Input
)

open class IntellijPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = IntellijProjectCompilerExtension(project)
    project.extensions.add(IntellijProjectCompilerExtension.id, extension)

    project.pluginManager.apply("org.metaborg.gradle.config.java-library")
    project.pluginManager.apply("org.jetbrains.intellij")

    configureIntellijLanguageProjectTask(project, extension)
    configureCompilerTask(project, extension)
  }

  private fun configureIntellijLanguageProjectTask(project: Project, extension: IntellijProjectCompilerExtension) {
    val configureIntellijProjectTask = project.tasks.register("configureIntellijProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
        finalized.compiler.getDependencies(input).forEach {
          it.addToDependencies(project)
        }
        project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
          exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
        })
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureIntellijProjectTask)
  }

  private fun configureCompilerTask(project: Project, extension: IntellijProjectCompilerExtension) {
    val compileTask = project.tasks.register("spoofaxCompileIntellijProject") {
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

    // Make all of IntelliJ's tasks depend on our task, because we generate Java code and a plugin.xml file.
    project.tasks.getByName(IntelliJPlugin.BUILD_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PATCH_PLUGIN_XML_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PREPARE_SANDBOX_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.RUN_IDE_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PUBLISH_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.VERIFY_PLUGIN_TASK_NAME).dependsOn(compileTask)
  }
}
