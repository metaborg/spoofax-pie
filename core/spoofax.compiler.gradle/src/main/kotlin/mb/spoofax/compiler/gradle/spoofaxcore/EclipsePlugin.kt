@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.coronium.plugin.BundleExtension
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class EclipseProjectCompilerSettings(
  val compiler: EclipseProjectCompiler.Input.Builder = EclipseProjectCompiler.Input.builder()
) {
  internal fun createInput(
    shared: Shared,
    project: GradleProject,
    adapterProjectCompilerInput: AdapterProjectCompiler.Input,
    eclipseExternaldepsDependency: GradleDependency
  ): EclipseProjectCompiler.Input {
    return this.compiler
      .shared(shared)
      .project(project)
      .eclipseExternaldepsDependency(eclipseExternaldepsDependency)
      .adapterProjectCompilerInput(adapterProjectCompilerInput)
      .build()
  }
}

open class EclipseProjectCompilerExtension(
  objects: ObjectFactory,
  project: Project,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<EclipseProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "eclipseProjectCompiler"
  }

  init {
    settings.convention(EclipseProjectCompilerSettings())
  }

  internal val input: EclipseProjectCompiler.Input by lazy {
    settings.finalizeValue()
    settings.get().createInput(
      compilerExtension.shared,
      project.toSpoofaxCompilerProject(),
      compilerExtension.adapterProjectCompilerExtension.input,
      compilerExtension.eclipseExternaldepsCompilerExtension.project.asProjectDependency()
    )
  }
}

open class EclipsePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = EclipseProjectCompilerExtension(project.objects, project, compilerExtension)
    project.extensions.add(EclipseProjectCompilerExtension.id, extension)

    project.gradle.projectsEvaluated {
      afterEvaluate(project, compilerExtension, extension)
    }

    /*
    HACK: apply plugins eagerly, otherwise their 'afterEvaluate' will not be triggered and the plugin will do nothing.
    Ensure that plugins are applied after we add a 'projectsEvaluated' listener, to ensure that our listener gets
    executed before those of the following plugins.
    */
    project.plugins.apply("org.metaborg.gradle.config.java-library")
    project.plugins.apply("org.metaborg.coronium.bundle")
  }

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: EclipseProjectCompilerExtension) {
    val compiler = compilerExtension.eclipseProjectCompiler
    val resourceService = compilerExtension.resourceService
    val input = extension.input
    val compilerProject = input.project()
    project.configureGroup(compilerProject)
    project.configureVersion(compilerProject)
    project.configureGeneratedSources(compilerProject, resourceService)
    compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    configureCompilerTask(project, input, compiler, resourceService)
    configureBundle(project, input, compiler, resourceService)
  }

  private fun configureCompilerTask(
    project: Project,
    input: EclipseProjectCompiler.Input,
    compiler: EclipseProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileEclipseProject") {
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

  private fun configureBundle(
    project: Project,
    input: EclipseProjectCompiler.Input,
    compiler: EclipseProjectCompiler,
    resourceService: ResourceService
  ) {
    project.configure<BundleExtension> {
      manifestFile = resourceService.toLocalFile(input.manifestMfFile())!!
      compiler.getBundleDependencies(input).forEach {
        it.caseOf()
          .bundle { dep, reexport -> requireBundle(dep.toGradleDependency(project), reexport) }
          .embeddingBundle { dep, reexport -> requireEmbeddingBundle(dep.toGradleDependency(project), reexport) }
          .targetPlatform { name, version, reexport -> requireTargetPlatform(name, version, reexport) }
      }
    }
  }
}
