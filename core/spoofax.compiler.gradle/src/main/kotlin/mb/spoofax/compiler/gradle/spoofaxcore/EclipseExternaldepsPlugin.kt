@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import aQute.bnd.gradle.BundleTaskConvention
import mb.coronium.plugin.EmbeddingExtension
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

open class EclipseExternaldepsProjectSettings(
  val adapterGradleProject: Project,
  val builder: EclipseExternaldepsProjectCompiler.Input.Builder = EclipseExternaldepsProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): EclipseExternaldepsProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterGradleProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val input = builder
      .shared(languageProjectFinalized.shared)
      .project(gradleProject.toSpoofaxCompilerProject())
      .languageProjectDependency(languageProjectFinalized.input.languageProject().project().asProjectDependency())
      .adapterProjectDependency(adapterProjectFinalized.input.adapterProject().project().asProjectDependency())
      .build()

    return EclipseExternaldepsProjectFinalized(input, languageProjectFinalized.compilers)
  }
}

open class EclipseExternaldepsProjectExtension(project: Project) {
  val settings: Property<EclipseExternaldepsProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "spoofaxEclipseExternaldepsProject"
  }

  internal val finalizedProvider: Provider<EclipseExternaldepsProjectFinalized> = project.providers.provider { finalized }
  internal val inputProvider: Provider<EclipseExternaldepsProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: EclipseExternaldepsProjectFinalized by lazy {
    project.logger.lifecycle("Finalizing Spoofax language Eclipse external dependencies project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Spoofax language Eclipse external dependencies project settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class EclipseExternaldepsProjectFinalized(
  val input: EclipseExternaldepsProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.eclipseExternaldepsProjectCompiler
}

@Suppress("unused")
open class EclipseExternaldepsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = EclipseExternaldepsProjectExtension(project)
    project.extensions.add(EclipseExternaldepsProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")
    project.plugins.apply("biz.aQute.bnd.builder")
    project.plugins.apply("org.metaborg.coronium.embedding")

    configureProjectTask(project, extension)
    configureCompilerTask(project, extension)
    configureJarTask(project, extension)
  }

  private fun configureProjectTask(project: Project, extension: EclipseExternaldepsProjectExtension) {
    val configureTask = project.tasks.register("spoofaxConfigureEclipseExternaldepsProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
        finalized.compiler.getDependencies(input).forEach {
          it.addToDependencies(project)
        }
        project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
          exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
        })
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureTask)
  }

  private fun configureCompilerTask(project: Project, extension: EclipseExternaldepsProjectExtension) {
    val compileTask = project.tasks.register("spoofaxCompileEclipseExternaldepsProject") {
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

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureJarTask(project: Project, extension: EclipseExternaldepsProjectExtension) {
    project.tasks.named<Jar>("jar").configure {
      inputs.property("input", extension.inputProvider)

      withConvention(BundleTaskConvention::class) {
        // Let BND use the runtime classpath, since this bundle is used for bundling runtime dependencies.
        setClasspath(sourceSet.runtimeClasspath)
      }

      // TODO: is doFirst OK here?
      doFirst {
        val finalized = extension.finalized

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
}
