@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.coronium.plugin.BundleExtension
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class EclipseProjectSettings(
  val builder: EclipseProjectCompiler.Input.Builder = EclipseProjectCompiler.Input.builder()
) {
  internal fun finalize(project: Project, adapterProject: Project, eclipseExternaldepsProject: Project): EclipseProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = this.builder
      .shared(languageProjectFinalized.shared)
      .project(project.toSpoofaxCompilerProject())
      .eclipseExternaldepsDependency(eclipseExternaldepsProject.toSpoofaxCompilerProject().asProjectDependency())
      .languageProjectCompilerInput(languageProjectFinalized.input)
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return EclipseProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class EclipseProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val eclipseExternaldepsProject: Property<Project> = project.objects.property()
  val settings: Property<EclipseProjectSettings> = project.objects.property()

  init {
    settings.convention(EclipseProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxEclipseProject"
    private const val name = "Spoofax language Eclipse project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }

  internal val eclipseExternaldepsProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's external dependencies project reference in $project")
    eclipseExternaldepsProject.finalizeValue()
    if(!eclipseExternaldepsProject.isPresent) {
      throw GradleException("$name's external dependencies project reference in $project has not been set")
    }
    eclipseExternaldepsProject.get()
  }

  internal val finalized: EclipseProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, adapterProjectFinalized, eclipseExternaldepsProjectFinalized)
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

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("org.metaborg.coronium.bundle")

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        extension.eclipseExternaldepsProjectFinalized.whenEclipseExternaldepsProjectFinalized {
          configure(project, extension.finalized)
        }
      }
    }
  }

  private fun configure(project: Project, finalized: EclipseProjectFinalized) {
    configureProject(project, finalized)
    configureCompilerTask(project, finalized)
    configureBundle(project, finalized)
  }

  private fun configureProject(project: Project, finalized: EclipseProjectFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompilerTask(project: Project, finalized: EclipseProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileEclipseProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureBundle(project: Project, finalized: EclipseProjectFinalized) {
    val input = finalized.input
    project.configure<BundleExtension> {
      manifestFile.set(finalized.resourceService.toLocalFile(input.manifestMfFile())!!)
    }
    configureBundleDependencies(project, finalized.compiler.getBundleDependencies(input))
  }
}

internal fun configureBundleDependencies(project: Project, dependencies: List<GradleConfiguredBundleDependency>) {
  project.configure<BundleExtension> {
    dependencies.forEach {
      it.caseOf()
        .bundleApi { dep -> project.dependencies.add("bundleApi", dep.toGradleDependency(project)) }
        .bundleImplementation { dep -> project.dependencies.add("bundleImplementation", dep.toGradleDependency(project)) }
        .bundleEmbedApi { dep -> project.dependencies.add("bundleEmbedApi", dep.toGradleDependency(project)) }
        .bundleEmbedImplementation { dep -> project.dependencies.add("bundleEmbedImplementation", dep.toGradleDependency(project)) }
        .bundleTargetPlatformApi { name, version -> project.dependencies.add("bundleTargetPlatformApi", createEclipseTargetPlatformDependency(name, version)) }
        .bundleTargetPlatformImplementation { name, version -> project.dependencies.add("bundleTargetPlatformImplementation", createEclipseTargetPlatformDependency(name, version)) }
    }
  }
}
