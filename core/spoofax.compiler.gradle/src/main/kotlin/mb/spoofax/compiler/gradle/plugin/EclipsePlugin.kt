@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.common.option.Option
import mb.common.result.Result
import mb.coronium.plugin.BundleExtension
import mb.pie.api.ValueSupplier
import mb.pie.dagger.PieComponent
import mb.resource.dagger.ResourceServiceComponent
import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

open class EclipseProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
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

  internal val compilerInputFinalized: EclipseProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    compilerInput.get()
      .shared(shared)
      .project(project.toSpoofaxCompilerProject())
      .packageId(EclipseProjectCompiler.Input.Builder.defaultPackageId(shared))
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
        val components = extension.languageProjectExtension.components
        configure(project, components.resourceServiceComponent, components.component, components.pieComponent, extension.compilerInputFinalized)
      }
    }
  }

  private fun configure(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    pieComponent: PieComponent,
    input: EclipseProjectCompiler.Input
  ) {
    configureProject(project, resourceServiceComponent, component, input)
    configureCompilerTask(project, resourceServiceComponent, component, pieComponent, input)
    configureBundle(project, resourceServiceComponent, component, input)
    configureJarTask(project, component, input)
  }

  private fun configureProject(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: EclipseProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
    project.addMainResourceDirectory(input.generatedResourcesDirectory(), resourceServiceComponent.resourceService)
    component.eclipseProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompilerTask(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    pieComponent: PieComponent,
    input: EclipseProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileEclipseProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.resources().map { resourceServiceComponent.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
        project.deleteDirectory(input.generatedResourcesDirectory(), resourceServiceComponent.resourceService)
        synchronized(pieComponent.pie) {
          pieComponent.pie.newSession().use { session ->
            session.require(component.eclipseProjectCompiler.createTask(ValueSupplier(Result.ofOk<Option<EclipseProjectCompiler.Input>, Exception>(Option.ofSome(input)))))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureBundle(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: EclipseProjectCompiler.Input
  ) {
    project.configure<BundleExtension> {
      manifestFile.set(resourceServiceComponent.resourceService.toLocalFile(input.manifestMfFile())!!)
    }
    configureBundleDependencies(project, component.eclipseProjectCompiler.getBundleDependencies(input))
  }

  private fun configureJarTask(
    project: Project,
    component: SpoofaxCompilerComponent,
    input: EclipseProjectCompiler.Input
  ) {
    project.tasks.named<Jar>("jar").configure {
      inputs.property("input", input)
      val exportPackages = component.eclipseProjectCompiler.getExportPackages(input).map { "$it.*" }.toCollection(LinkedHashSet())
      val existingExportPackages = manifest.attributes.get("Export-Package")
      if(existingExportPackages != null) {
        exportPackages.add(existingExportPackages.toString())
      }
      val privatePackages = component.eclipseProjectCompiler.getPrivatePackages(input).map { "$it.*" }.toCollection(LinkedHashSet())
      val existingPrivatePackages = manifest.attributes.get("Private-Package")
      if(existingPrivatePackages != null) {
        privatePackages.add(existingPrivatePackages.toString())
      }
      manifest {
        attributes(
          Pair("Export-Package", exportPackages.joinToString(", ")),
          Pair("Private-Package", privatePackages.joinToString(", "))
        )
      }
    }
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
