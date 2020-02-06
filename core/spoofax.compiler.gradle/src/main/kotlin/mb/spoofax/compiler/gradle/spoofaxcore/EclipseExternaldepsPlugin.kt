@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import aQute.bnd.gradle.BundleTaskConvention
import mb.coronium.plugin.EmbeddingExtension
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

open class EclipseExternaldepsProjectCompilerSettings(
  val compiler: EclipseExternaldepsProjectCompiler.Input.Builder = EclipseExternaldepsProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, project: GradleProject, languageProjectDependency: GradleDependency, adapterProjectDependency: GradleDependency): EclipseExternaldepsProjectCompiler.Input {
    return this.compiler.shared(shared).project(project).languageProjectDependency(languageProjectDependency).adapterProjectDependency(adapterProjectDependency).build()
  }
}

open class EclipseExternaldepsProjectCompilerExtension(
  objects: ObjectFactory,
  project: Project,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<EclipseExternaldepsProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "eclipseExternaldepsProjectCompiler"
  }

  init {
    settings.convention(EclipseExternaldepsProjectCompilerSettings())
  }

  internal val input: EclipseExternaldepsProjectCompiler.Input by lazy {
    settings.finalizeValue()
    settings.get().createInput(compilerExtension.shared, project.toSpoofaxCompilerProject(), compilerExtension.languageProjectCompilerExtension.project.asProjectDependency(), compilerExtension.adapterProjectCompilerExtension.project.asProjectDependency())
  }
}

open class EclipseExternaldepsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = EclipseExternaldepsProjectCompilerExtension(project.objects, project, compilerExtension)
    project.extensions.add(EclipseExternaldepsProjectCompilerExtension.id, extension)
    project.gradle.projectsEvaluated {
      afterEvaluate(project, compilerExtension, extension)
    }
  }

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: EclipseExternaldepsProjectCompilerExtension) {
    val compiler = compilerExtension.eclipseExternaldepsProjectCompiler
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
    configureEmbedding(project, input)
  }

  private fun configureCompilerTask(
    project: Project,
    input: EclipseExternaldepsProjectCompiler.Input,
    compiler: EclipseExternaldepsProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileEclipseExternaldepsProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureEmbedding(
    project: Project,
    input: EclipseExternaldepsProjectCompiler.Input
  ) {
    project.plugins.apply("biz.aQute.bnd.builder")
    project.plugins.apply("org.metaborg.coronium.embedding")

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
      "*;provider=${input.project().coordinate().artifactId()};mandatory:=provider"
    )

    project.tasks.named<Jar>("jar").configure {
      withConvention(BundleTaskConvention::class) {
        // Let BND use the runtime classpath, since this bundle is used for bundling runtime dependencies.
        setClasspath(sourceSet.runtimeClasspath)
      }
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
