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

open class AdapterProjectCompilerSettings(
  val rootGradleProject: Project,
  val languageGradleProject: Project,
  val adapterProject: AdapterProject.Builder = AdapterProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.AdapterProjectInput.Builder = ClassloaderResourcesCompiler.AdapterProjectInput.builder(),
  val parser: ParserCompiler.AdapterProjectInput.Builder = ParserCompiler.AdapterProjectInput.builder(),
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.AdapterProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val compiler: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): AdapterProjectCompilerFinalized {
    val project = gradleProject.toSpoofaxCompilerProject()
    val spoofaxCompilerExtension: SpoofaxCompilerExtension = rootGradleProject.extensions.getByType()
    val shared = spoofaxCompilerExtension.shared
    val languageProjectCompilerExtension: LanguageProjectCompilerExtension = languageGradleProject.extensions.getByType()
    val languageProjectCompilerInput = languageProjectCompilerExtension.finalized.input
    val languageProjectDependency = languageGradleProject.toSpoofaxCompilerProject().asProjectDependency()

    val adapterProject = this.adapterProject.shared(shared).project(project).build()
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

    val compiler = this.compiler
      .shared(shared)
      .adapterProject(adapterProject)
      .classloaderResources(classloaderResources)
      .parser(parser)
      .languageProjectDependency(languageProjectDependency)
    if(styler != null) {
      compiler.styler(styler)
    }
    if(completer != null) {
      compiler.completer(completer)
    }
    if(strategoRuntime != null) {
      compiler.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      compiler.constraintAnalyzer(constraintAnalyzer)
    }
    val input = compiler.build()

    val resourceService = spoofaxCompilerExtension.resourceService
    val adapterProjectCompiler = spoofaxCompilerExtension.adapterProjectCompiler
    return AdapterProjectCompilerFinalized(resourceService, adapterProjectCompiler, input)
  }
}

open class AdapterProjectCompilerExtension(project: Project) {
  val settings: Property<AdapterProjectCompilerSettings> = project.objects.property()

  companion object {
    internal const val id = "adapterProjectCompiler"
  }

  internal val finalizedProvider: Provider<AdapterProjectCompilerFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<AdapterProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: AdapterProjectCompilerFinalized by lazy {
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Adapter project compiler settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class AdapterProjectCompilerFinalized(
  val resourceService: ResourceService,
  val compiler: AdapterProjectCompiler,
  val input: AdapterProjectCompiler.Input
)

@Suppress("unused")
open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = AdapterProjectCompilerExtension(project)
    project.extensions.add(AdapterProjectCompilerExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")

    configureAdapterLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
  }

  private fun configureAdapterLanguageProjectTask(project: Project, extension: AdapterProjectCompilerExtension) {
    val configureAdapterProjectTask = project.tasks.register("configureAdapterProject") {
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
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureAdapterProjectTask)
  }

  private fun configureCompileTask(project: Project, extension: AdapterProjectCompilerExtension) {
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
