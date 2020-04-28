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
  val adapterGradleProject: Project,
  val eclipseExternaldepsGradleProject: Project,
  val builder: EclipseProjectCompiler.Input.Builder = EclipseProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): EclipseProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterGradleProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = this.builder
      .shared(languageProjectFinalized.shared)
      .project(gradleProject.toSpoofaxCompilerProject())
      .eclipseExternaldepsDependency(eclipseExternaldepsGradleProject.toSpoofaxCompilerProject().asProjectDependency())
      .adapterProjectCompilerInput(adapterProjectExtension.finalized.input)
      .build()

    return EclipseProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class EclipseProjectExtension(project: Project) {
  val settings: Property<EclipseProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "spoofaxEclipseProject"
  }

  internal val finalizedProvider: Provider<EclipseProjectFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<EclipseProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: EclipseProjectFinalized by lazy {
    project.logger.lifecycle("Finalizing Spoofax language Eclipse project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Spoofax language Eclipse project settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class EclipseProjectFinalized(
  val input: EclipseProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.eclipseProjectCompiler
}

open class EclipsePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = EclipseProjectExtension(project)
    project.extensions.add(EclipseProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")

    configureProjectTask(project, extension)
    configureCompilerTask(project, extension)

    project.plugins.apply("org.metaborg.coronium.bundle")

    // HACK: configure coronium plugin after all projects have been evaluated.
    project.afterEvaluate {
      //configureBundle(project, extension)
    }
  }

  private fun configureProjectTask(project: Project, extension: EclipseProjectExtension) {
    val configureTask = project.tasks.register("spoofaxConfigureEclipseProject") {
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
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureTask)
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
    // HACK: finalize in configuration phase, but after all projects have been evaluated.
    val finalized = extension.finalized
    val input = finalized.input
    project.configure<BundleExtension> {
      manifestFile = finalized.resourceService.toLocalFile(input.manifestMfFile())!!
      finalized.compiler.getBundleDependencies(input).forEach {
        it.caseOf()
          .bundle { dep, reexport -> requireBundle(dep.toGradleDependency(project), reexport) }
          .embeddingBundle { dep, reexport -> requireEmbeddingBundle(dep.toGradleDependency(project), reexport) }
          .targetPlatform { name, version, reexport -> requireTargetPlatform(name, version, reexport) }
      }
    }
  }
}
