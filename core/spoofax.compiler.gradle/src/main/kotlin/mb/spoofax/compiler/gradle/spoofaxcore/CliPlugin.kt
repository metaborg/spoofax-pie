@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class CliProjectCompilerSettings(
  val compiler: CliProjectCompiler.Input.Builder = CliProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, project: GradleProject, adapterProjectCompilerInput: AdapterProjectCompiler.Input): CliProjectCompiler.Input {
    return this.compiler.shared(shared).project(project).adapterProjectCompilerInput(adapterProjectCompilerInput).build()
  }
}

open class CliProjectCompilerExtension(
  objects: ObjectFactory,
  project: Project,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<CliProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "cliProjectCompiler"
  }

  init {
    settings.convention(CliProjectCompilerSettings())
  }

  internal val input: CliProjectCompiler.Input by lazy {
    settings.finalizeValue()
    settings.get().createInput(compilerExtension.shared, project.toSpoofaxCompilerProject(), compilerExtension.adapterProjectCompilerExtension.input)
  }
}

open class CliPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = CliProjectCompilerExtension(project.objects, project, compilerExtension)
    project.extensions.add(CliProjectCompilerExtension.id, extension)

    project.gradle.projectsEvaluated {
      afterEvaluate(project, compilerExtension, extension)
    }

    /*
    HACK: apply plugins eagerly, otherwise their 'afterEvaluate' will not be triggered and the plugin will do nothing.
    Ensure that plugins are applied after we add a 'projectsEvaluated' listener, to ensure that our listener gets
    executed before those of the following plugins.
    */
    project.plugins.apply("org.metaborg.gradle.config.java-application")
  }

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: CliProjectCompilerExtension) {
    val compiler = compilerExtension.cliProjectCompiler
    val resourceService = compilerExtension.resourceService
    val input = extension.input
    val compilerProject = input.project()
    project.configureGroup(compilerProject)
    project.configureVersion(compilerProject)
    project.configureGeneratedSources(compilerProject, resourceService)
    compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.configure<JavaApplication> {
      mainClassName = input.main().qualifiedId()
    }
    configureCompilerTask(project, input, compiler, resourceService)
  }

  private fun configureCompilerTask(
    project: Project,
    input: CliProjectCompiler.Input,
    compiler: CliProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileCliProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), resourceService)
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
