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

open class AdapterProjectExtension(project: Project) {
  val languageProject: Property<Project> = project.objects.property()
  val adapterProject: Property<AdapterProject.Builder> = project.objects.property()
  val compilerInput: Property<AdapterProjectCompilerInputBuilder> = project.objects.property()

  fun adapterProject(closure: AdapterProject.Builder.() -> Unit) {
    adapterProject.get().closure()
  }

  fun compilerInput(closure: AdapterProjectCompilerInputBuilder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    adapterProject.convention(AdapterProject.builder().project(project.toSpoofaxCompilerProject()))
    compilerInput.convention(AdapterProjectCompilerInputBuilder())
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
  internal val languageProjectExtension get() = languageProjectFinalized.extensions.getByType<LanguageProjectExtension>()

  internal val adapterProjectFinalized: AdapterProject by lazy {
    project.logger.debug("Finalizing $name's project in $project")
    adapterProject.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    adapterProject.get()
      .packageId(AdapterProject.Builder.defaultPackageId(shared))
      .shared(shared)
      .build()
  }

  internal val compilerInputFinalized: AdapterProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    compilerInput.get().build(languageProjectExtension.compilerInputFinalized, adapterProjectFinalized)
  }
}

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
        configure(project, extension.languageProjectExtension.component, extension.compilerInputFinalized)
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, input: AdapterProjectCompiler.Input) {
    configureProject(project, component, input)
    configureCompileTask(project, component, input)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, input: AdapterProjectCompiler.Input) {
    project.addMainJavaSourceDirectory(input.adapterProject().generatedJavaSourcesDirectory(), component.resourceService)
    component.adapterProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, component: SpoofaxCompilerGradleComponent, input: AdapterProjectCompiler.Input) {
    val compileTask = project.tasks.register("compileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.adapterProject().generatedJavaSourcesDirectory(), component.resourceService)
        component.pie.newSession().use { session ->
          session.require(component.adapterProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
