@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceRuntimeException
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

open class LanguageProjectCompilerSettings(
  val languageProject: LanguageProject.Builder = LanguageProject.builder(),
  val parser: ParserCompiler.LanguageProjectInput.Builder = ParserCompiler.LanguageProjectInput.builder(),
  val styler: StylerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.LanguageProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.LanguageProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val compiler: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, project: GradleProject): LanguageProjectCompiler.Input {
    val languageProject = this.languageProject.shared(shared).project(project).build()
    val parser = this.parser.shared(shared).languageProject(languageProject).build()
    val styler = if(this.styler != null) this.styler.shared(shared).languageProject(languageProject).build() else null
    val completer = if(this.completer != null) this.completer.shared(shared).languageProject(languageProject).build() else null
    val strategoRuntime = if(this.strategoRuntime != null) this.strategoRuntime.shared(shared).languageProject(languageProject).build() else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) this.constraintAnalyzer.shared(shared).languageProject(languageProject).build() else null
    val compiler = this.compiler.shared(shared).languageProject(languageProject).parser(parser)
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

open class LanguageProjectCompilerExtension(
  objects: ObjectFactory,
  compilerExtension: SpoofaxCompilerExtension
) {
  val settings: Property<LanguageProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "languageProjectCompiler"
  }

  init {
    settings.convention(LanguageProjectCompilerSettings())
  }

  internal val project by lazy {
    compilerExtension.languageGradleProject.finalizeValue()
    if(!compilerExtension.languageGradleProject.isPresent) {
      throw GradleException("Language project was not set")
    }
    compilerExtension.languageGradleProject.get().toSpoofaxCompilerProject()
  }

  internal val input: LanguageProjectCompiler.Input by lazy {
    settings.finalizeValue()
    settings.get().createInput(compilerExtension.shared, project)
  }
}

open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val compilerExtension = project.extensions.getByType<SpoofaxCompilerExtension>()
    val extension = LanguageProjectCompilerExtension(project.objects, compilerExtension)
    project.extensions.add(LanguageProjectCompilerExtension.id, extension)
    compilerExtension.languageGradleProject.set(project)

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

  private fun afterEvaluate(project: Project, compilerExtension: SpoofaxCompilerExtension, extension: LanguageProjectCompilerExtension) {
    val compiler = compilerExtension.languageProjectCompiler
    val resourceService = compilerExtension.resourceService
    val input = extension.input
    val compilerProject = input.languageProject().project();
    project.configureGroup(compilerProject)
    project.configureVersion(compilerProject)
    project.configureGeneratedSources(compilerProject, resourceService)
    compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    configureCompilerTask(project, input, compiler, resourceService)
    configureCopySpoofaxLanguageTasks(project, input, compiler)
  }

  private fun configureCompilerTask(
    project: Project,
    input: LanguageProjectCompiler.Input,
    compiler: LanguageProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.languageProject().project(), resourceService)
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(
    project: Project,
    input: LanguageProjectCompiler.Input,
    compiler: LanguageProjectCompiler
  ) {
    val destinationPackage = input.languageProject().packagePath()
    val includeStrategoClasses = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
    val includeStrategoJavastratClasses = input.strategoRuntime().map { it.copyJavaStrategyClasses() }.orElse(false)
    val copyResources = compiler.getCopyResources(input)

    // Create language specification dependency and 'spoofaxLanguage' configuration that contains this dependency.
    val configuration = project.configurations.create("spoofaxLanguage") {
      val dependency: Dependency = input.languageSpecificationDependency().caseOf()
        .project { configureSpoofaxLanguageDependency(project.dependencies.project(it)) }
        .module { configureSpoofaxLanguageDependency(project.dependencies.module(it.toGradleNotation()) as ModuleDependency) }
        .files { project.dependencies.create(project.files(it)) }
      dependencies.add(dependency)
    }

    // Unpack the '.spoofax-language' archive.
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      inputs.property("input", input)
      dependsOn(configuration)
      from({ configuration.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
      into(unpackSpoofaxLanguageDir)

      val allCopyResources = copyResources.toMutableList()
      if(includeStrategoClasses) {
        allCopyResources.add("target/metaborg/stratego.jar")
      }
      if(includeStrategoJavastratClasses) {
        allCopyResources.add("target/metaborg/stratego-javastrat.jar")
      }
      include(allCopyResources)
    }

    // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
    val resourcesCopySpec = project.copySpec {
      from(unpackSpoofaxLanguageDir)
      include(copyResources)
    }
    val strategoCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
      exclude("META-INF")
    }
    val strategoJavastratCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego-javastrat.jar"))
      exclude("META-INF")
    }
    val copyMainTask = project.tasks.register<Copy>("copyMainResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["main"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
      if(includeStrategoJavastratClasses) {
        into(".") { with(strategoJavastratCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
    val copyTestTask = project.tasks.register<Copy>("copyTestResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["test"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
      if(includeStrategoJavastratClasses) {
        into(".") { with(strategoJavastratCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
  }

  private fun configureSpoofaxLanguageDependency(dependency: ModuleDependency): Dependency {
    dependency.targetConfiguration = Dependency.DEFAULT_CONFIGURATION
    dependency.isTransitive = false // Don't care about transitive dependencies, just want the '.spoofax-language' artifact.
    dependency.artifact {
      name = dependency.name
      type = "spoofax-language"
      extension = "spoofax-language"
    }
    return dependency
  }
}
