@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProject
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

open class LanguageProjectCompilerSettings(
  val rootGradleProject: Project,
  val languageProject: LanguageProject.Builder = LanguageProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.LanguageProjectInput.Builder = ClassloaderResourcesCompiler.LanguageProjectInput.builder(),
  val parser: ParserCompiler.LanguageProjectInput.Builder = ParserCompiler.LanguageProjectInput.builder(),
  val styler: StylerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.LanguageProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.LanguageProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val compiler: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): LanguageProjectCompilerFinalized {
    val project = gradleProject.toSpoofaxCompilerProject()
    val rootProjectExtension: RootProjectExtension = rootGradleProject.extensions.getByType()
    val shared = rootProjectExtension.shared

    val languageProject = this.languageProject.shared(shared).project(project).build()
    val classloaderResources = this.classloaderResources.shared(shared).languageProject(languageProject).build()
    val parser = this.parser.shared(shared).languageProject(languageProject).build()
    val styler = if(this.styler != null) this.styler.shared(shared).languageProject(languageProject).build() else null
    val completer = if(this.completer != null) this.completer.shared(shared).languageProject(languageProject).build() else null
    val strategoRuntime = if(this.strategoRuntime != null) this.strategoRuntime.shared(shared).languageProject(languageProject).build() else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) this.constraintAnalyzer.shared(shared).languageProject(languageProject).build() else null

    val compiler = this.compiler
      .shared(shared)
      .languageProject(languageProject)
      .classloaderResources(classloaderResources)
      .parser(parser)
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

    val resourceService = rootProjectExtension.resourceService
    val languageProjectCompiler = rootProjectExtension.languageProjectCompiler
    return LanguageProjectCompilerFinalized(resourceService, languageProjectCompiler, input)
  }
}

open class LanguageProjectCompilerExtension(project: Project) {
  val settings: Property<LanguageProjectCompilerSettings> = project.objects.property()

  companion object {
    internal const val id = "languageProjectCompiler"
  }

  internal val finalizedProvider: Provider<LanguageProjectCompilerFinalized> = settings.map { it.finalize(project) }
  internal val inputProvider: Provider<LanguageProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: LanguageProjectCompilerFinalized by lazy {
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Language project compiler settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class LanguageProjectCompilerFinalized(
  val resourceService: ResourceService,
  val compiler: LanguageProjectCompiler,
  val input: LanguageProjectCompiler.Input
) {
  val destinationPackage: String get() = input.languageProject().packagePath()
  val includeStrategoClasses: Boolean get() = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
  val includeStrategoJavastratClasses: Boolean get() = input.strategoRuntime().map { it.copyJavaStrategyClasses() }.orElse(false)
  val copyResources: ArrayList<String> get() = compiler.getCopyResources(input)
}

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = LanguageProjectCompilerExtension(project)
    project.extensions.add(LanguageProjectCompilerExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")

    configureConfigureLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
    configureCopySpoofaxLanguageTasks(project, extension)
  }

  private fun configureConfigureLanguageProjectTask(project: Project, extension: LanguageProjectCompilerExtension) {
    val configureLanguageProjectTask = project.tasks.register("configureLanguageProject") {
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
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(configureLanguageProjectTask)
  }

  private fun configureCompileTask(project: Project, extension: LanguageProjectCompilerExtension) {
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)
      outputs.files(extension.resourceServiceProvider.flatMap { resourceService -> extension.inputProvider.map { input -> input.providedFiles().map { resourceService.toLocalFile(it) } } })

      doLast {
        val finalized = extension.finalized
        val input = finalized.input
        project.deleteGenSourceSpoofaxDirectory(input.languageProject().project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(project: Project, extension: LanguageProjectCompilerExtension) {
    // Create language specification dependency and 'spoofaxLanguage' configuration that contains this dependency.
    val configuration = project.configurations.create("spoofaxLanguage") {}
    val populateSpoofaxLanguageConfigurationTask = project.tasks.register("populateSpoofaxLanguageConfiguration") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)

      doLast {
        val finalized = extension.finalized
        val dependency: Dependency = finalized.input.languageSpecificationDependency().caseOf()
          .project { configureSpoofaxLanguageDependency(project.dependencies.project(it)) }
          .module { configureSpoofaxLanguageDependency(project.dependencies.module(it.toGradleNotation()) as ModuleDependency) }
          .files { project.dependencies.create(project.files(it)) }
        configuration.dependencies.add(dependency)
      }
    }

    // Unpack the '.spoofax-language' archive.
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      group = "spoofax compiler"
      inputs.property("input", extension.inputProvider)
      dependsOn(populateSpoofaxLanguageConfigurationTask, configuration)
      from({ configuration.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
      into(unpackSpoofaxLanguageDir)

      // TODO: is it OK to do doFirst here?
      doFirst {
        val finalized = extension.finalized
        val allCopyResources = finalized.copyResources.toMutableList()
        if(finalized.includeStrategoClasses) {
          allCopyResources.add("target/metaborg/stratego.jar")
        }
        if(finalized.includeStrategoJavastratClasses) {
          allCopyResources.add("target/metaborg/stratego-javastrat.jar")
        }
        include(allCopyResources)
      }
    }

    // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
    val strategoCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
      exclude("META-INF")
    }
    val strategoJavastratCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego-javastrat.jar"))
      exclude("META-INF")
    }
    val copyMainTask = project.tasks.register<Copy>("copyMainResources") {
      group = "spoofax compiler"
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["main"].java.outputDir)

      // TODO: is it OK to do doFirst here?
      doFirst {
        val finalized = extension.finalized
        into(finalized.destinationPackage) {
          from(unpackSpoofaxLanguageDir)
          include(finalized.copyResources)
        }
        if(finalized.includeStrategoClasses) {
          into(".") { with(strategoCopySpec) }
        }
        if(finalized.includeStrategoJavastratClasses) {
          into(".") { with(strategoJavastratCopySpec) }
        }
      }
    }
    project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
    val copyTestTask = project.tasks.register<Copy>("copyTestResources") {
      group = "spoofax compiler"
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["test"].java.outputDir)

      // TODO: is it OK to do doFirst here?
      doFirst {
        val finalized = extension.finalized
        into(finalized.destinationPackage) {
          from(unpackSpoofaxLanguageDir)
          include(finalized.copyResources)
        }
        if(finalized.includeStrategoClasses) {
          into(".") { with(strategoCopySpec) }
        }
        if(finalized.includeStrategoJavastratClasses) {
          into(".") { with(strategoJavastratCopySpec) }
        }
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
