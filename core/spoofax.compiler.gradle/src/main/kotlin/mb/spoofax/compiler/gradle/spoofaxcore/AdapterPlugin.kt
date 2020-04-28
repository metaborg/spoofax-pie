@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.AdapterProject
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*

open class AdapterProjectSettings(
  val languageGradleProject: Project,

  val adapterProject: AdapterProject.Builder = AdapterProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.AdapterProjectInput.Builder = ClassloaderResourcesCompiler.AdapterProjectInput.builder(),
  val parser: ParserCompiler.AdapterProjectInput.Builder = ParserCompiler.AdapterProjectInput.builder(),
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.AdapterProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional

  val builder: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): AdapterProjectFinalized {
    val languageProjectExtension: LanguageProjectExtension = languageGradleProject.extensions.getByType()
    val languageProjectFinalized = languageProjectExtension.finalized
    val shared = languageProjectFinalized.shared
    val languageProjectCompilerInput = languageProjectFinalized.input

    val adapterProject = this.adapterProject.shared(shared).project(gradleProject.toSpoofaxCompilerProject()).build()
    val classloaderResources = this.classloaderResources.languageProjectInput(languageProjectCompilerInput.classloaderResources()).build()
    val parser = this.parser.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.parser()).build()
    val styler = if(this.styler != null) {
      if(!languageProjectCompilerInput.styler().isPresent) {
        throw GradleException("Styler adapter project input is present, but styler language project input is not")
      }
      this.styler.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.styler().get()).build()
    } else null
    val completer = if(this.completer != null) {
      if(!languageProjectCompilerInput.completer().isPresent) {
        throw GradleException("Completer adapter project input is present, but completer language project input is not")
      }
      this.completer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.completer().get()).build()
    } else null
    val strategoRuntime = if(this.strategoRuntime != null) {
      if(!languageProjectCompilerInput.strategoRuntime().isPresent) {
        throw GradleException("Stratego runtime adapter project input is present, but Stratego runtime language project input is not")
      }
      this.strategoRuntime.languageProjectInput(languageProjectCompilerInput.strategoRuntime().get()).build()
    } else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) {
      if(!languageProjectCompilerInput.constraintAnalyzer().isPresent) {
        throw GradleException("Constraint analyzer adapter project input is present, but constraint analyzer runtime language project input is not")
      }
      this.constraintAnalyzer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.constraintAnalyzer().get()).build()
    } else null

    val builder = this.builder
      .shared(shared)
      .adapterProject(adapterProject)
      .classloaderResources(classloaderResources)
      .parser(parser)
      .languageProjectDependency(languageGradleProject.toSpoofaxCompilerProject().asProjectDependency())
    if(styler != null) {
      builder.styler(styler)
    }
    if(completer != null) {
      builder.completer(completer)
    }
    if(strategoRuntime != null) {
      builder.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      builder.constraintAnalyzer(constraintAnalyzer)
    }
    val input = builder.build()

    return AdapterProjectFinalized(input, languageProjectFinalized)
  }
}

open class AdapterProjectExtension(project: Project) {
  val settings: Property<AdapterProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "spoofaxAdapterProject"
  }

  internal val finalizedProvider: Provider<AdapterProjectFinalized> = project.providers.provider { finalized }
  internal val inputProvider: Provider<AdapterProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: AdapterProjectFinalized by lazy {
    project.logger.lifecycle("Finalizing Spoofax language adapter project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Spoofax language Adapter project settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class AdapterProjectFinalized(
  val input: AdapterProjectCompiler.Input,
  val languageProjectFinalized: LanguageProjectFinalized
) {
  val compilers = languageProjectFinalized.compilers
  val resourceService = compilers.resourceService
  val compiler = compilers.adapterProjectCompiler
}

@Suppress("unused")
open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = AdapterProjectExtension(project)
    project.extensions.add(AdapterProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")

    configureAdapterLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
  }

  private fun configureAdapterLanguageProjectTask(project: Project, extension: AdapterProjectExtension) {
    val configureTask = project.tasks.register("spoofaxConfigureAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
        finalized.compiler.getDependencies(finalized.input).forEach {
          it.addToDependencies(project)
        }
      }
    }

    // Make compileJava depend on our task, because we configure source sets and dependencies.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureTask)
  }

  private fun configureCompileTask(project: Project, extension: AdapterProjectExtension) {
    val compileTask = project.tasks.register("spoofaxCompileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)
      outputs.files(extension.resourceServiceProvider.flatMap { resourceService -> extension.inputProvider.map { input -> input.providedFiles().map { resourceService.toLocalFile(it) } } })

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.deleteGenSourceSpoofaxDirectory(input.adapterProject().project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
