@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.coronium.plugin.BundleExtension
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class EclipseProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val eclipseExternaldepsProject: Property<Project> = project.objects.property()
  val compilerInput: Property<EclipseProjectCompiler.Input.Builder> = project.objects.property()

  fun compilerInput(closure: EclipseProjectCompiler.Input.Builder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(EclipseProjectCompiler.Input.builder())
  }

  companion object {
    internal const val id = "languageEclipseProject"
    private const val name = "language Eclipse project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }
  internal val adapterProjectExtension get() = adapterProjectFinalized.extensions.getByType<AdapterProjectExtension>()
  internal val languageProjectExtension get() = adapterProjectExtension.languageProjectExtension

  internal val eclipseExternaldepsProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's external dependencies project reference in $project")
    eclipseExternaldepsProject.finalizeValue()
    if(!eclipseExternaldepsProject.isPresent) {
      throw GradleException("$name's external dependencies project reference in $project has not been set")
    }
    eclipseExternaldepsProject.get()
  }

  internal val compilerInputFinalized: EclipseProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    compilerInput.get()
      .shared(shared)
      .project(project.toSpoofaxCompilerProject())
      .packageId(EclipseProjectCompiler.Input.Builder.defaultPackageId(shared))
      .eclipseExternaldepsDependency(eclipseExternaldepsProjectFinalized.toSpoofaxCompilerProject().asProjectDependency())
      .languageProjectCompilerInput(languageProjectExtension.compilerInputFinalized)
      .adapterProjectCompilerInput(adapterProjectExtension.compilerInputFinalized)
      .build()
  }
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
          configure(project, extension.languageProjectExtension.component, extension.compilerInputFinalized)
        }
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseProjectCompiler.Input) {
    configureProject(project, component, input)
    configureCompilerTask(project, component, input)
    configureBundle(project, component, input)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseProjectCompiler.Input) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), component.resourceService)
    component.eclipseProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompilerTask(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseProjectCompiler.Input) {
    val compileTask = project.tasks.register("spoofaxCompileEclipseProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), component.resourceService)
        component.pie.newSession().use { session ->
          session.require(component.eclipseProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureBundle(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseProjectCompiler.Input) {
    project.configure<BundleExtension> {
      manifestFile.set(component.resourceService.toLocalFile(input.manifestMfFile())!!)
    }
    configureBundleDependencies(project, component.eclipseProjectCompiler.getBundleDependencies(input))
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
