@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

open class EclipseExternaldepsProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val compilerInput: Property<EclipseExternaldepsProjectCompiler.Input.Builder> = project.objects.property()

  fun compilerInput(closure: EclipseExternaldepsProjectCompiler.Input.Builder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(EclipseExternaldepsProjectCompiler.Input.builder())
  }

  companion object {
    internal const val id = "languageEclipseExternaldepsProject"
    private const val name = "language Eclipse external dependencies project"
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

  internal val compilerInputFinalized: EclipseExternaldepsProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    compilerInput.get()
      .shared(shared)
      .project(project.toSpoofaxCompilerProject())
      .packageId(EclipseExternaldepsProjectCompiler.Input.Builder.defaultPackageId(shared))
      .adapterProjectCompilerInput(adapterProjectExtension.compilerInputFinalized)
      .build()
  }
}

internal fun Project.whenEclipseExternaldepsProjectFinalized(closure: () -> Unit) = whenFinalized<EclipseExternaldepsProjectExtension>(closure)

@Suppress("unused")
open class EclipseExternaldepsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = EclipseExternaldepsProjectExtension(project)
    project.extensions.add(EclipseExternaldepsProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("org.metaborg.coronium.bundle")

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.languageProjectExtension.component, extension.compilerInputFinalized)
      }
    }
  }

  private fun configure(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseExternaldepsProjectCompiler.Input) {
    configureProject(project, component, input)
    configureBundle(project, component, input)
    configureCompilerTask(project, component, input)
    configureJarTask(project, input)
  }

  private fun configureProject(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseExternaldepsProjectCompiler.Input) {
    component.eclipseExternaldepsProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureBundle(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseExternaldepsProjectCompiler.Input) {
    configureBundleDependencies(project, component.eclipseExternaldepsProjectCompiler.getBundleDependencies(input))
  }

  private fun configureCompilerTask(project: Project, component: SpoofaxCompilerGradleComponent, input: EclipseExternaldepsProjectCompiler.Input) {
    val compileTask = project.tasks.register("compileEclipseExternaldepsProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { component.resourceService.toLocalFile(it) })

      doLast {
        component.pie.newSession().use { session ->
          session.require(component.eclipseExternaldepsProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureJarTask(project: Project, input: EclipseExternaldepsProjectCompiler.Input) {
    project.tasks.named<Jar>("jar").configure {
      inputs.property("input", input)

      val exports = listOf(
        // Provided by 'javax.inject' bundle.
        "!javax.inject.*",
        // Provided by 'spoofax.eclipse.externaldeps' bundle.
        "!mb.log.*",
        "!mb.resource.*",
        "!mb.pie.api.*",
        "!mb.pie.runtime.*",
        "!mb.common.*",
        "!mb.spoofax.core.*",
        "!dagger.*",
        // Do not export testing packages.
        "!junit.*",
        "!org.junit.*",
        // Do not export compile-time annotation packages.
        "!org.checkerframework.*",
        "!org.codehaus.mojo.animal_sniffer.*",
        // Allow split package for 'mb.nabl2'.
        "mb.nabl2.*;-split-package:=merge-first",
        // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
        "*;provider=${input.project().coordinate().artifactId()};mandatory:=provider"
      )
      manifest {
        attributes(
          Pair("Export-Package", exports.joinToString(", "))
        )
      }
    }
  }
}
