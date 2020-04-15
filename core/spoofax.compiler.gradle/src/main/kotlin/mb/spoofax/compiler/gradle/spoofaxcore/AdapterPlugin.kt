@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.AdapterProject
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class AdapterProjectCompilerSettings(
  val adapterProject: AdapterProject.Builder = AdapterProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.AdapterProjectInput.Builder = ClassloaderResourcesCompiler.AdapterProjectInput.builder(),
  val parser: ParserCompiler.AdapterProjectInput.Builder = ParserCompiler.AdapterProjectInput.builder(),
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.AdapterProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val compiler: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, languageProjectInput: LanguageProjectCompiler.Input, project: GradleProject, languageProjectDependency: GradleDependency): AdapterProjectCompiler.Input {
    val adapterProject = this.adapterProject.shared(shared).project(project).build()
    val classloaderResources = this.classloaderResources.languageProjectInput(languageProjectInput.classloaderResources()).build()
    val parser = this.parser.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.parser()).build()
    val styler = if(this.styler != null) {
      if(!languageProjectInput.styler().isPresent) {
        throw GradleException("Styler adapter project input is present, but styler language project input is not")
      }
      this.styler.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.styler().get()).build()
    } else null
    val completer = if(this.completer != null) {
      if(!languageProjectInput.completer().isPresent) {
        throw GradleException("Completer adapter project input is present, but completer language project input is not")
      }
      this.completer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.completer().get()).build()
    } else null
    val strategoRuntime = if(this.strategoRuntime != null) {
      if(!languageProjectInput.strategoRuntime().isPresent) {
        throw GradleException("Stratego runtime adapter project input is present, but Stratego runtime language project input is not")
      }
      this.strategoRuntime.languageProjectInput(languageProjectInput.strategoRuntime().get()).build()
    } else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) {
      if(!languageProjectInput.constraintAnalyzer().isPresent) {
        throw GradleException("Constraint analyzer adapter project input is present, but constraint analyzer runtime language project input is not")
      }
      this.constraintAnalyzer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.constraintAnalyzer().get()).build()
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
    return compiler.build()
  }
}

open class AdapterProjectCompilerExtension(
  objects: ObjectFactory,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<AdapterProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "adapterProjectCompiler"
  }

  init {
    settings.convention(AdapterProjectCompilerSettings())
  }

  internal val project by lazy {
    compilerExtension.adapterGradleProject.finalizeValue()
    if(!compilerExtension.adapterGradleProject.isPresent) {
      throw GradleException("Adapter project was not set")
    }
    compilerExtension.adapterGradleProject.get().toSpoofaxCompilerProject()
  }

  internal val input: AdapterProjectCompiler.Input by lazy {
    settings.finalizeValue()
    val languageProjectExtension = compilerExtension.languageProjectCompilerExtension
    settings.get().createInput(compilerExtension.shared, languageProjectExtension.input, project, languageProjectExtension.project.asProjectDependency())
  }
}

open class AdapterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = AdapterProjectCompilerExtension(project.objects, compilerExtension)
    project.extensions.add(AdapterProjectCompilerExtension.id, extension)
    compilerExtension.adapterGradleProject.set(project)

    project.gradle.projectsEvaluated {
      afterEvaluate(project, compilerExtension, extension)
    }

    /*
    HACK: apply plugins eagerly, otherwise their 'afterEvaluate' will not be triggered and the plugin will do nothing.
    Ensure that plugins are applied after we add a 'projectsEvaluated' listener, to ensure that our listener gets
    executed before those of the following plugins.
    */
    project.plugins.apply("org.metaborg.gradle.config.java-library")
  }

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: AdapterProjectCompilerExtension) {
    val compiler = compilerExtension.adapterProjectCompiler
    val resourceService = compilerExtension.resourceService
    val input = extension.input
    val compilerProject = input.adapterProject().project();
    project.configureGroup(compilerProject)
    project.configureVersion(compilerProject)
    project.configureGeneratedSources(compilerProject, resourceService)
    compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    configureCompilerTask(project, input, compiler, resourceService)
  }

  private fun configureCompilerTask(
    project: Project,
    input: AdapterProjectCompiler.Input,
    compiler: AdapterProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.adapterProject().project(), resourceService)
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
