@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.spoofaxcore.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class AdapterProjectSettings(
  val adapterProject: AdapterProject.Builder = AdapterProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.AdapterProjectInput.Builder = ClassloaderResourcesCompiler.AdapterProjectInput.builder(),
  val parser: ParserCompiler.AdapterProjectInput.Builder? = null, // Optional
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.AdapterProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val multilangAnalyzer: MultilangAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional

  val builder: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
) {
  internal fun finalize(project: Project, languageProject: Project): AdapterProjectFinalized {
    val languageProjectExtension: LanguageProjectExtension = languageProject.extensions.getByType()
    val languageProjectFinalized = languageProjectExtension.settingsFinalized
    val shared = languageProjectFinalized.shared
    val languageProjectCompilerInput = languageProjectFinalized.input

    val adapterProject = this.adapterProject.shared(shared).project(project.toSpoofaxCompilerProject()).build()
    val classloaderResources = this.classloaderResources.languageProjectInput(languageProjectCompilerInput.classloaderResources()).build()
    val styler = if(this.styler != null) {
      if(!languageProjectCompilerInput.styler().isPresent) {
        throw GradleException("Styler adapter project input is present, but styler language project input is not")
      }
      this.styler.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.styler().get()).build()
    } else null
    val parser = if(this.parser != null) {
      if(!languageProjectCompilerInput.styler().isPresent) {
        throw GradleException("Parser adapter project input is present, but parser language project input is not")
      }
      this.parser.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.parser().get()).build()
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
    val multilangAnalyzer = if(this.multilangAnalyzer != null) {
      if(!languageProjectCompilerInput.multilangAnalyzer().isPresent) {
        throw GradleException("Constraint analyzer adapter project input is present, but constraint analyzer runtime language project input is not")
      }
      this.multilangAnalyzer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectCompilerInput.multilangAnalyzer().get()).build()
    } else null

    val builder = this.builder
      .shared(shared)
      .adapterProject(adapterProject)
      .classloaderResources(classloaderResources)
      .languageProjectDependency(languageProject.toSpoofaxCompilerProject().asProjectDependency())
    if(parser != null) {
      builder.parser(parser)
    }
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
    if(multilangAnalyzer != null) {
      builder.multilangAnalyzer(multilangAnalyzer)
    }
    val input = builder.build()

    return AdapterProjectFinalized(input, languageProjectFinalized)
  }
}

open class AdapterProjectExtension(project: Project) {
  val languageProject: Property<Project> = project.objects.property()
  val settings: Property<AdapterProjectSettings> = project.objects.property()

  init {
    settings.convention(AdapterProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxAdapterProject"
    private const val name = "Spoofax language adapter project"
  }

  internal val languageProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's language project reference in $project")
    languageProject.finalizeValue()
    if(!languageProject.isPresent) {
      throw GradleException("$name's language project reference in $project has not been set")
    }
    languageProject.get()
  }

  internal val finalized: AdapterProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, languageProjectFinalized)
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

internal fun Project.whenAdapterProjectFinalized(closure: () -> Unit) = whenFinalized<AdapterProjectExtension> {
  val extension : AdapterProjectExtension = extensions.getByType()
  extension.languageProjectFinalized.whenLanguageProjectFinalized(closure)
}

@Suppress("unused")
open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = AdapterProjectExtension(project)
    project.extensions.add(AdapterProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)

    project.afterEvaluate {
      extension.languageProjectFinalized.whenLanguageProjectFinalized {
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: AdapterProjectFinalized) {
    configureProject(project, finalized)
    configureCompileTask(project, finalized)
  }

  private fun configureProject(project: Project, finalized: AdapterProjectFinalized) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(finalized.input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, finalized: AdapterProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.adapterProject().project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
