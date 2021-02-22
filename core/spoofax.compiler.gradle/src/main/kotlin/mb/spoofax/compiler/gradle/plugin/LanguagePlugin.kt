@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.log.dagger.DaggerLoggerComponent
import mb.log.dagger.LoggerModule
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.PieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.dagger.DaggerRootResourceServiceComponent
import mb.resource.dagger.ResourceServiceComponent
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.nio.charset.StandardCharsets

open class Components {
  val loggerComponent = DaggerLoggerComponent.builder()
    .loggerModule(LoggerModule.stdErrErrorsAndWarnings())
    .build()
  val resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
    .loggerComponent(loggerComponent)
    .build()
  val component = DaggerSpoofaxCompilerComponent.builder()
    .spoofaxCompilerModule(SpoofaxCompilerModule(TemplateCompiler(StandardCharsets.UTF_8)))
    .loggerComponent(loggerComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .build()
  val pieComponent = DaggerRootPieComponent.builder()
    .loggerComponent(loggerComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .rootPieModule(RootPieModule { PieBuilderImpl() }.addTaskDefsFrom(component))
    .build()
}

open class LanguageProjectExtension(project: Project) {
  val shared: Property<Shared.Builder> = project.objects.property()
  val languageProject: Property<LanguageProject.Builder> = project.objects.property()
  val compilerInput: Property<LanguageProjectCompilerInputBuilder> = project.objects.property()
  val compilerInputConfigurationClosures: ListProperty<LanguageProjectCompilerInputBuilder.() -> Unit> = project.objects.listProperty()
  val statixDependencies: ListProperty<Project> = project.objects.listProperty() // statixDependencies must be in a separate property, since its finalized value is used to check if the settings property can be finalized

  fun shared(closure: Shared.Builder.() -> Unit) {
    shared.get().closure()
  }

  fun languageProject(closure: LanguageProject.Builder.() -> Unit) {
    languageProject.get().closure()
  }

  fun compilerInput(closure: LanguageProjectCompilerInputBuilder.() -> Unit) {
    compilerInputConfigurationClosures.add { closure() }
  }

  init {
    shared.convention(Shared.builder())
    languageProject.convention(LanguageProject.builder()
      .project(project.toSpoofaxCompilerProject())
    )
    compilerInput.convention(LanguageProjectCompilerInputBuilder())
  }

  companion object {
    internal const val id = "languageProject"
    private const val name = "language project"
  }

  val components: Components by lazy { Components() }

  val sharedFinalized: Shared by lazy {
    project.logger.debug("Finalizing $name shared settings in $project")
    shared.finalizeValue()

    val properties = project.loadLockFileProperties()
    val shared = shared.get()
      .withPersistentProperties(properties)
      .build()
    shared.savePersistentProperties(properties)
    project.saveLockFileProperties(properties)

    shared
  }

  val languageProjectFinalized: LanguageProject by lazy {
    project.logger.debug("Finalizing $name's project in $project")
    languageProject.finalizeValue()
    languageProject.get()
      .packageId(LanguageProject.Builder.defaultPackageId(sharedFinalized))
      .build()
  }

  val compilerInputFinalized: LanguageProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val languageProjectBuilder = compilerInput.get()

    project.logger.debug("Finalizing $name's compiler input configuration closures in $project")
    compilerInputConfigurationClosures.finalizeValue()
    compilerInputConfigurationClosures.get().forEach { languageProjectBuilder.it() }

    val statixDependencies = statixDependenciesFinalized
    val multilangAnalyzer = languageProjectBuilder.multilangAnalyzer
    if(multilangAnalyzer == null) {
      if(statixDependencies.isNotEmpty()) {
        project.logger.warn("Statix dependencies given, but no multilang analyzer configuration set. Ignoring statix dependencies")
      }
    } else {
      statixDependencies.forEach {
        val ext: LanguageProjectExtension = it.extensions.getByType()
        val factory = ext.compilerInputFinalized.multilangAnalyzer().get().specConfigFactory()
        multilangAnalyzer.addDependencyFactories(factory)
      }
    }

    languageProjectBuilder.build(sharedFinalized, languageProjectFinalized)
  }

  val statixDependenciesFinalized: List<Project> by lazy {
    project.logger.debug("Finalizing $name statix dependencies in $project")
    statixDependencies.finalizeValue()
    statixDependencies.get()
  }
}

fun Project.whenLanguageProjectFinalized(closure: () -> Unit) = whenFinalized(LanguageProjectExtension::class.java) {
  val extension: LanguageProjectExtension = extensions.getByType()
  // Project is fully finalized only iff all dependencies are finalized as well
  extension.statixDependenciesFinalized.whenAllLanguageProjectsFinalized(closure)
}

fun List<Project>.whenAllLanguageProjectsFinalized(closure: () -> Unit) {
  if(isEmpty()) {
    // No dependencies to wait for, so execute immediately
    closure()
  } else {
    // After first project in list is finalized, invoke wait for the others
    first().whenLanguageProjectFinalized {
      drop(1).whenAllLanguageProjectsFinalized(closure)
    }
  }
}

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = LanguageProjectExtension(project)
    project.extensions.add(LanguageProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)

    project.afterEvaluate {
      project.whenLanguageProjectFinalized {
        extension.statixDependenciesFinalized.whenAllLanguageProjectsFinalized {
          val components = extension.components
          configure(project, components.resourceServiceComponent, components.component, components.pieComponent, extension.compilerInputFinalized)
        }
      }
    }
  }

  private fun configure(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    pieComponent: PieComponent,
    input: LanguageProjectCompiler.Input
  ) {
    configureProject(project, resourceServiceComponent, component, input)
    configureCompileTask(project, resourceServiceComponent, component, pieComponent, input)
  }

  private fun configureProject(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: LanguageProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.languageProject().generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
    component.languageProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    pieComponent: PieComponent,
    input: LanguageProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.javaSourceFiles().map { resourceServiceComponent.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.languageProject().generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
        synchronized(pieComponent.pie) {
          pieComponent.pie.newSession().use { session ->
            session.require(component.languageProjectCompiler.createTask(input))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
