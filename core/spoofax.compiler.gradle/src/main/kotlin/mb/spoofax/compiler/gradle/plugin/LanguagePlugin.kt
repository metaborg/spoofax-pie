@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.common.util.Properties
import mb.pie.runtime.PieBuilderImpl
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.io.IOException
import java.nio.charset.StandardCharsets

open class LanguageProjectSettings {
  val shared: Shared.Builder = Shared.builder()
  val builder: LanguageProjectBuilder = LanguageProjectBuilder()

  internal fun finalize(
    gradleProject: Project,
    configurationClosures: MutableList<(LanguageProjectSettings) -> Unit>,
    component: SpoofaxCompilerGradleComponent
  ): LanguageProjectFinalized {
    // Apply configuration closures to ourselves before finalizing.
    configurationClosures.forEach { it(this) }

    // Attempt to load compiler properties from file.
    val spoofaxCompilerPropertiesFile = gradleProject.projectDir.resolve("spoofaxc.lock")
    val spoofaxCompilerProperties = Properties()
    if(spoofaxCompilerPropertiesFile.exists()) {
      spoofaxCompilerPropertiesFile.bufferedReader().use {
        try {
          spoofaxCompilerProperties.load(it)
        } catch(e: IOException) {
          gradleProject.logger.warn("Failed to load Spoofax compiler properties from file '$spoofaxCompilerPropertiesFile'", e)
        }
      }
    }

    // Build shared settings.
    val shared = shared
      .withPersistentProperties(spoofaxCompilerProperties)
      .build()

    // Build language project input.
    builder.project
      .project(gradleProject.toSpoofaxCompilerProject())
      .packageId(LanguageProject.Builder.defaultPackageId(shared))
    val input = builder.build(shared)

    // Save compiler properties to file.
    shared.savePersistentProperties(spoofaxCompilerProperties)
    spoofaxCompilerPropertiesFile.parentFile.mkdirs()
    spoofaxCompilerPropertiesFile.createNewFile()
    spoofaxCompilerPropertiesFile.bufferedWriter().use {
      try {
        spoofaxCompilerProperties.storeWithoutDate(it)
        it.flush()
      } catch(e: IOException) {
        gradleProject.logger.warn("Failed to save Spoofax compiler properties to file '$spoofaxCompilerPropertiesFile'", e)
      }
    }

    return LanguageProjectFinalized(shared, input, component)
  }

  internal fun addStatixDependencies(statixDependencies: List<Project>) {
    statixDependencies.forEach {
      val ext: LanguageProjectExtension = it.extensions.getByType()
      val factory = ext.settingsFinalized.input.multilangAnalyzer().get().specConfigFactory()
      this.builder.multilangAnalyzer?.addDependencyFactories(factory)
    }
  }
}

open class LanguageProjectExtension(project: Project) {
  val settings: Property<LanguageProjectSettings> = project.objects.property()
  val settingsConfigurationClosures: ListProperty<(LanguageProjectSettings) -> Unit> = project.objects.listProperty()
  val statixDependencies: ListProperty<Project> = project.objects.listProperty() // statixDependencies must be in a separate property, since its finalized value is used to check if the settings property can be finalized

  init {
    settings.convention(LanguageProjectSettings())
  }

  companion object {
    internal const val id = "languageProject"
    private const val name = "language project"
  }

  val component = DaggerSpoofaxCompilerGradleComponent.builder()
    .spoofaxCompilerModule(SpoofaxCompilerModule(TemplateCompiler(StandardCharsets.UTF_8)))
    .spoofaxCompilerGradleModule(SpoofaxCompilerGradleModule { PieBuilderImpl() })
    .build()

  val settingsFinalized: LanguageProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    val settings = settings.get()
    val statixDependencies = statixDependenciesFinalized
    if(settings.builder.multilangAnalyzer == null) {
      if(statixDependencies.isNotEmpty()) {
        project.logger.warn("Statix dependencies given, but no multilang analyzer configuration set. Ignoring statix dependencies")
      }
    } else {
      settings.addStatixDependencies(statixDependencies)
    }
    settingsConfigurationClosures.finalizeValue()
    settings.finalize(project, settingsConfigurationClosures.get(), component)
  }

  val statixDependenciesFinalized: List<Project> by lazy {
    project.logger.debug("Finalizing $name statix dependencies in $project")
    statixDependencies.finalizeValue()
    if(!statixDependencies.isPresent) {
      throw GradleException("$name statix dependencies in $project have not been set")
    }
    statixDependencies.get()
  }
}

class LanguageProjectFinalized(
  val shared: Shared,
  val input: LanguageProjectCompiler.Input,
  val component: SpoofaxCompilerGradleComponent
)

fun Project.whenLanguageProjectFinalized(closure: () -> Unit) = whenFinalized<LanguageProjectExtension> {
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
      extension.statixDependenciesFinalized.whenAllLanguageProjectsFinalized {
        configure(project, extension.component, extension.settingsFinalized)
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, finalized: LanguageProjectFinalized) {
    configureProject(project, component, finalized)
    configureCompileTask(project, component, finalized)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, finalized: LanguageProjectFinalized) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), component.resourceService)
    component.languageProjectCompiler.getDependencies(finalized.input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, component: SpoofaxCompilerGradleComponent, finalized: LanguageProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.languageProject().project(), component.resourceService)
        component.pie.newSession().use { session ->
          session.require(component.languageProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
