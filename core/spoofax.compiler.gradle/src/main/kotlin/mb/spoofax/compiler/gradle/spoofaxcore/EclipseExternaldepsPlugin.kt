@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import aQute.bnd.gradle.BundleTaskConvention
import mb.coronium.plugin.EmbeddingExtension
import mb.spoofax.compiler.spoofaxcore.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
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

    project.plugins.apply("org.metaborg.gradle.config.java-library")
    project.plugins.apply("biz.aQute.bnd.builder")
    project.plugins.apply("org.metaborg.coronium.embedding")

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    configureProjectTask(project, finalized)
    configureCompilerTask(project, finalized)
    configureJarTask(project, finalized)
  }

  private fun configureProjectTask(project: Project, finalized: EclipseExternaldepsProjectFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
      exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
    })
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

      withConvention(BundleTaskConvention::class) {
        // Let BND use the runtime classpath, since this bundle is used for bundling runtime dependencies.
        setClasspath(sourceSet.runtimeClasspath)
      }
      // Use bnd to create a single OSGi bundle JAR that includes all dependencies.
      val requires = listOf(
        "javax.inject", // Depends on javax.inject bundle provided by Eclipse.
        "spoofax.eclipse.externaldeps" // Depends on external dependencies from spoofax.eclipse.
      )
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
        // Allow split package for 'mb.nabl'.
        "mb.nabl2.*;-split-package:=merge-first",
        // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
        "*;provider=${finalized.input.project().coordinate().artifactId()};mandatory:=provider"
      )
      manifest {
        attributes(
          Pair("Bundle-Vendor", project.group),
          Pair("Bundle-SymbolicName", project.name),
          Pair("Bundle-Name", project.name),
          Pair("Bundle-Version", project.the<EmbeddingExtension>().bundleVersion),

          Pair("Require-Bundle", requires.joinToString(", ")),
          Pair("Import-Package", ""), // Disable imports

          Pair("Export-Package", exports.joinToString(", ")),

          Pair("-nouses", "true"), // Disable 'uses' directive generation for exports.
          Pair("-nodefaultversion", "true") // Disable 'version' directive generation for exports.
        )
      }
    }
  }
}
