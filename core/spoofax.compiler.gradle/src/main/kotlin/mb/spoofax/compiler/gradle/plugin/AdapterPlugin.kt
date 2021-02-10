@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.common.option.Option
import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.core.platform.ResourceServiceComponent
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

  internal val languageProjectFinalized: Project? by lazy {
    project.logger.debug("Finalizing $name's language project reference in $project")
    languageProject.finalizeValue()
    if(languageProject.isPresent) {
      languageProject.get()
    } else {
      null
    }
  }
  internal val isSeparateProject: Boolean by lazy {
    languageProjectFinalized != null
  }
  internal val languageOrThisProjectFinalized: Project by lazy {
    languageProjectFinalized ?: project
  }
  internal val languageProjectExtension get() = languageOrThisProjectFinalized.extensions.getByType<LanguageProjectExtension>()

  internal val adapterProjectFinalized: AdapterProject by lazy {
    project.logger.debug("Finalizing $name's project in $project")
    adapterProject.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    adapterProject.get()
      .packageId(if(!isSeparateProject) AdapterProject.Builder.defaultPackageId(shared) else AdapterProject.Builder.defaultSeparatePackageId(shared))
      .shared(shared)
      .build()
  }

  internal val compilerInputFinalized: AdapterProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val languageProjectDependency = Option.ofNullable(languageProjectFinalized).map { it.toSpoofaxCompilerProject().asProjectDependency() }
    compilerInput.get().build(languageProjectExtension.compilerInputFinalized, languageProjectDependency, adapterProjectFinalized)
  }
}

internal fun Project.whenAdapterProjectFinalized(closure: () -> Unit) = whenFinalized(AdapterProjectExtension::class.java) {
  val extension: AdapterProjectExtension = extensions.getByType()
  // Adapter project is only fully finalized when its dependent language project is finalized as well.
  extension.languageOrThisProjectFinalized.whenLanguageProjectFinalized(closure)
}

@Suppress("unused")
open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = AdapterProjectExtension(project)
    project.extensions.add(AdapterProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)

    project.afterEvaluate {
      extension.languageOrThisProjectFinalized.whenLanguageProjectFinalized {
        configure(project, extension.languageProjectExtension.resourceServiceComponent, extension.languageProjectExtension.component, extension.compilerInputFinalized)
      }
    }
  }

  private fun configure(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: AdapterProjectCompiler.Input
  ) {
    configureProject(project, resourceServiceComponent, component, input)
    configureCompileTask(project, resourceServiceComponent, component, input)
  }

  private fun configureProject(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: AdapterProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.adapterProject().generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
    component.adapterProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: AdapterProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.javaSourceFiles().map { resourceServiceComponent.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.adapterProject().generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
        synchronized(component.pie) {
          component.pie.newSession().use { session ->
            session.require(component.adapterProjectCompiler.createTask(input))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
