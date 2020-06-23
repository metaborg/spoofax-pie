@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.spoofaxcore.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

open class EclipseExternaldepsProjectSettings(
  val builder: EclipseExternaldepsProjectCompiler.Input.Builder = EclipseExternaldepsProjectCompiler.Input.builder()
) {
  internal fun finalize(project: Project, adapterProject: Project): EclipseExternaldepsProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = builder
      .shared(languageProjectFinalized.shared)
      .project(project.toSpoofaxCompilerProject())
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .languageProjectDependency(languageProjectFinalized.input.languageProject().project().asProjectDependency())
      .adapterProjectDependency(adapterProjectFinalized.input.adapterProject().project().asProjectDependency())
      .build()

    return EclipseExternaldepsProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class EclipseExternaldepsProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val settings: Property<EclipseExternaldepsProjectSettings> = project.objects.property()

  init {
    settings.convention(EclipseExternaldepsProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxEclipseExternaldepsProject"
    private const val name = "Spoofax Eclipse external dependencies project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }

  internal val finalized: EclipseExternaldepsProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, adapterProjectFinalized)
  }
}

internal class EclipseExternaldepsProjectFinalized(
  val input: EclipseExternaldepsProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.eclipseExternaldepsProjectCompiler
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
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    configureProject(project, finalized)
    configureBundle(project, finalized)
    configureCompilerTask(project, finalized)
    configureJarTask(project, finalized)
  }

  private fun configureProject(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureBundle(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    configureBundleDependencies(project, finalized.compiler.getBundleDependencies(finalized.input))
  }

  private fun configureCompilerTask(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileEclipseExternaldepsProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureJarTask(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    project.tasks.named<Jar>("jar").configure {
      inputs.property("input", finalized.input)

      val exports = listOf(
        // Provided by 'javax.inject' bundle.
        "!javax.inject.*",
        // Provided by 'spoofax.eclipse.externaldeps' bundle.
        "!mb.log.*",
        "!mb.resource.*",
        "!mb.pie.*",
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
        "*;provider=${finalized.input.project().coordinate().artifactId()};mandatory:=provider"
      )
      manifest {
        attributes(
          Pair("Export-Package", exports.joinToString(", "))
        )
      }
    }
  }
}
