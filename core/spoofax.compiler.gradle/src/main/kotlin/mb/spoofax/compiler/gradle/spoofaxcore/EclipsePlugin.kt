@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.coronium.plugin.BundleExtension
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*

open class EclipseProjectSettings(
  val rootGradleProject: Project,
  val eclipseExternaldepsProject: Project,
  val adapterGradleProject: Project,
  val builder: EclipseProjectCompiler.Input.Builder = EclipseProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): EclipseProjectFinalized {
    val project = gradleProject.toSpoofaxCompilerProject()
    val rootProjectExtension: RootProjectExtension = rootGradleProject.extensions.getByType()
    val shared = rootProjectExtension.shared
    val adapterProjectExtension: AdapterProjectExtension = adapterGradleProject.extensions.getByType()

    val input = this.builder
      .shared(shared)
      .project(project)
      .eclipseExternaldepsDependency(eclipseExternaldepsProject.toSpoofaxCompilerProject().asProjectDependency())
      .adapterProjectCompilerInput(adapterProjectExtension.finalized.input)
      .build()

    return EclipseProjectFinalized(rootProjectExtension.resourceService, rootProjectExtension.eclipseProjectCompiler, input)
  }
}

open class EclipseProjectExtension(project: Project) {
  val settings: Property<EclipseProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "eclipseProjectCompiler"
  }

  internal val finalizedProvider: Provider<EclipseProjectFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<EclipseProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: EclipseProjectFinalized by lazy {
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Eclipse externaldeps project compiler settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class EclipseProjectFinalized(
  val resourceService: ResourceService,
  val compiler: EclipseProjectCompiler,
  val input: EclipseProjectCompiler.Input
)

open class EclipsePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = EclipseProjectExtension(project)
    project.extensions.add(EclipseProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")
    project.plugins.apply("org.metaborg.coronium.bundle")

    configureProjectTask(project, extension)
    configureCompilerTask(project, extension)
    configureBundle(project, extension)
  }

  private fun configureProjectTask(project: Project, extension: EclipseProjectExtension) {
    val configureProjectTask = project.tasks.register("configureProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
        finalized.compiler.getDependencies(input).forEach {
          it.addToDependencies(project)
        }
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureProjectTask)
  }

  private fun configureCompilerTask(project: Project, extension: EclipseProjectExtension) {
    val compileTask = project.tasks.register("spoofaxCompileEclipseProject") {
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

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureBundle(project: Project, extension: EclipseProjectExtension) {
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
