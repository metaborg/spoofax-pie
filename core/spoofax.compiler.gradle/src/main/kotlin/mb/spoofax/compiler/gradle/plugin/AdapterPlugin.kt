@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.gradle.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class AdapterProjectSettings {
  val builder: AdapterProjectBuilder = AdapterProjectBuilder()

  internal fun finalize(gradleProject: Project, languageProject: Project): AdapterProjectFinalized {
    val languageProjectFinalized = languageProject.extensions.getByType<LanguageProjectExtension>().settingsFinalized

    builder.project
      .project(gradleProject.toSpoofaxCompilerProject())
      .packageId(AdapterProject.Builder.defaultPackageId(languageProjectFinalized.shared))
    val input = builder.build(languageProjectFinalized.input)

    return AdapterProjectFinalized(input, languageProjectFinalized)
  }
}

open class AdapterProjectExtension(project: Project) {
  val languageProject: Property<Project> = project.objects.property()
  val settings: Property<AdapterProjectSettings> = project.objects.property()

  init {
    settings.convention(AdapterProjectSettings())
  }

  companion object {
    internal const val id = "languageAdapterProject"
    private const val name = "language adapter project"
  }

  internal val languageProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's language project reference in $project")
    languageProject.finalizeValue()
    if(!languageProject.isPresent) {
      throw GradleException("$name's language project reference in $project has not been set")
    }
    languageProject.get()
  }

  internal val finalized: AdapterProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, languageProjectFinalized)
  }
}

internal class AdapterProjectFinalized(
  val input: AdapterProjectCompiler.Input,
  val languageProjectFinalized: LanguageProjectFinalized
)

internal fun Project.whenAdapterProjectFinalized(closure: () -> Unit) = whenFinalized<AdapterProjectExtension> {
  val extension: AdapterProjectExtension = extensions.getByType()
  // Adapter project is only fully finalized when its dependent language project is finalized as well
  extension.languageProjectFinalized.whenLanguageProjectFinalized(closure)
}

@Suppress("unused")
open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = AdapterProjectExtension(project)
    project.extensions.add(AdapterProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)

    project.afterEvaluate {
      extension.languageProjectFinalized.whenLanguageProjectFinalized {
        configure(project, extension.finalized.languageProjectFinalized.component, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, finalized: AdapterProjectFinalized) {
    configureProject(project, component, finalized)
    configureCompileTask(project, component, finalized)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, finalized: AdapterProjectFinalized) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), component.resourceService)
    component.adapterProjectCompiler.getDependencies(finalized.input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, component: SpoofaxCompilerGradleComponent, finalized: AdapterProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.adapterProject().project(), component.resourceService)
        component.pie.newSession().use { session ->
          session.require(component.adapterProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
