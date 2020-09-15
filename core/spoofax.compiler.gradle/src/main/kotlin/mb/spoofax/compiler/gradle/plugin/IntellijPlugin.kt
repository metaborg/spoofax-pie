@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.IntelliJPlugin
import org.jetbrains.intellij.IntelliJPluginExtension

open class IntellijProjectSettings(
  val builder: IntellijProjectCompiler.Input.Builder = IntellijProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project, adapterProject: Project): IntellijProjectCompilerFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val shared = languageProjectFinalized.shared
    val project = gradleProject.toSpoofaxCompilerProject()
    val input = builder
      .shared(shared)
      .project(project)
      .packageId(IntellijProjectCompiler.Input.Builder.defaultPackageId(shared))
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return IntellijProjectCompilerFinalized(input, languageProjectFinalized)
  }
}

open class IntellijProjectCompilerExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val settings: Property<IntellijProjectSettings> = project.objects.property()

  init {
    settings.convention(IntellijProjectSettings())
  }

  companion object {
    internal const val id = "languageIntellijProject"
    private const val name = "language IntelliJ project"
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
  val languageProjectFinalized: LanguageProjectFinalized
)

open class IntellijPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = IntellijProjectCompilerExtension(project)
    project.extensions.add(IntellijProjectCompilerExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.pluginManager.apply("org.jetbrains.intellij")

    // Disable some IntelliJ plugin functionality to increase incrementality.
    project.configure<IntelliJPluginExtension> {
      instrumentCode = false
    }
    project.tasks.getByName("buildSearchableOptions").enabled = false

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.finalized.languageProjectFinalized.component, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, finalized: IntellijProjectCompilerFinalized) {
    configureProject(project, component, finalized)
    configureCompilerTask(project, component, finalized)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, finalized: IntellijProjectCompilerFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), component.resourceService)
    component.intellijProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
      exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
    })
  }

  private fun configureCompilerTask(project: Project, component: SpoofaxCompilerGradleComponent, finalized: IntellijProjectCompilerFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileIntellijProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), component.resourceService)
        component.pie.newSession().use { session ->
          session.require(component.intellijProjectCompiler.createTask(input))
        }
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
