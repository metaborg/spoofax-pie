@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.DefaultResourceService
import mb.resource.ResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProject
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.TemplateCompiler
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

open class Compilers {
  internal val resourceService = DefaultResourceService(FSResourceRegistry())
  internal val charset = StandardCharsets.UTF_8
  internal val templateCompiler = TemplateCompiler(Shared::class.java, resourceService, charset)
  internal val classloaderResourceService = ClassloaderResourcesCompiler(templateCompiler)
  internal val parserCompiler = ParserCompiler(templateCompiler)
  internal val stylerCompiler = StylerCompiler(templateCompiler)
  internal val completerCompiler = CompleterCompiler(templateCompiler)
  internal val strategoRuntimeCompiler = StrategoRuntimeCompiler(templateCompiler)
  internal val constraintAnalyzerCompiler = ConstraintAnalyzerCompiler(templateCompiler)
  internal val languageProjectCompiler = LanguageProjectCompiler(templateCompiler, classloaderResourceService, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val adapterProjectCompiler = AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val cliProjectCompiler = CliProjectCompiler(templateCompiler)
  internal val eclipseExternaldepsProjectCompiler = EclipseExternaldepsProjectCompiler(templateCompiler)
  internal val eclipseProjectCompiler = EclipseProjectCompiler(templateCompiler)
  internal val intellijProjectCompiler = IntellijProjectCompiler(templateCompiler)
}

open class LanguageProjectSettings(
  val shared: Shared.Builder = Shared.builder(),

  val languageProject: LanguageProject.Builder = LanguageProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.LanguageProjectInput.Builder = ClassloaderResourcesCompiler.LanguageProjectInput.builder(),
  val parser: ParserCompiler.LanguageProjectInput.Builder = ParserCompiler.LanguageProjectInput.builder(),
  val styler: StylerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val completer: CompleterCompiler.LanguageProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.LanguageProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.LanguageProjectInput.Builder? = null, // Optional

  val builder: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): LanguageProjectFinalized {
    // Attempt to load compiler properties from file.
    val spoofaxCompilerPropertiesFile = gradleProject.projectDir.resolve("spoofax_compiler.properties")
    val spoofaxCompilerProperties = Properties()
    if(spoofaxCompilerPropertiesFile.exists()) {
      spoofaxCompilerPropertiesFile.inputStream().use {
        try {
          spoofaxCompilerProperties.load(it)
        } catch(e: IOException) {
          gradleProject.logger.warn("Failed to load Spoofax compiler properties from file '$spoofaxCompilerPropertiesFile'", e)
        }
      }
    }

    // Build shared settings.
    val shared = shared
      .withPersistentProperties(spoofaxCompilerProperties)
      .baseDirectory(FSPath(gradleProject.projectDir.parent)) // TODO: remove the need to set a base directory, as it is often wrong.
      .build()

    // Build language project compiler settings.
    val languageProject = this.languageProject.shared(shared).project(gradleProject.toSpoofaxCompilerProject()).build()
    val classloaderResources = this.classloaderResources.shared(shared).languageProject(languageProject).build()
    val parser = this.parser.shared(shared).languageProject(languageProject).build()
    val styler = if(this.styler != null) this.styler.shared(shared).languageProject(languageProject).build() else null
    val completer = if(this.completer != null) this.completer.shared(shared).languageProject(languageProject).build() else null
    val strategoRuntime = if(this.strategoRuntime != null) this.strategoRuntime.shared(shared).languageProject(languageProject).build() else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) this.constraintAnalyzer.shared(shared).languageProject(languageProject).build() else null
    val builder = this.builder
      .shared(shared)
      .languageProject(languageProject)
      .classloaderResources(classloaderResources)
      .parser(parser)
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

    // Save compiler properties to file.
    shared.savePersistentProperties(spoofaxCompilerProperties)
    spoofaxCompilerPropertiesFile.parentFile.mkdirs()
    spoofaxCompilerPropertiesFile.createNewFile()
    spoofaxCompilerPropertiesFile.outputStream().use {
      try {
        spoofaxCompilerProperties.store(it, null)
        it.flush()
      } catch(e: IOException) {
        gradleProject.logger.warn("Failed to save Spoofax compiler properties to file '$spoofaxCompilerPropertiesFile'", e)
      }
    }

    return LanguageProjectFinalized(shared, input, Compilers())
  }
}

open class LanguageProjectExtension(project: Project) {
  val settings: Property<LanguageProjectSettings> = project.objects.property()

  companion object {
    internal const val id = "spoofaxLanguageProject"
  }

  internal val finalizedProvider: Provider<LanguageProjectFinalized> = project.providers.provider { finalized }
  internal val inputProvider: Provider<LanguageProjectCompiler.Input> = finalizedProvider.map { it.input }
  internal val resourceServiceProvider: Provider<ResourceService> = finalizedProvider.map { it.resourceService }

  internal val finalized: LanguageProjectFinalized by lazy {
    project.logger.lifecycle("Finalizing Spoofax language project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("Spoofax Language project settings have not been set")
    }
    settings.get().finalize(project)
  }
}

internal class LanguageProjectFinalized(
  val shared: Shared,
  val input: LanguageProjectCompiler.Input,
  val compilers: Compilers
) {
  val resourceService = compilers.resourceService
  val compiler = compilers.languageProjectCompiler

  val destinationPackage: String get() = input.languageProject().packagePath()
  val includeStrategoClasses: Boolean get() = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
  val includeStrategoJavastratClasses: Boolean get() = input.strategoRuntime().map { it.copyJavaStrategyClasses() }.orElse(false)
  val copyResources: ArrayList<String> get() = compiler.getCopyResources(input)
}

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = LanguageProjectExtension(project)
    project.extensions.add(LanguageProjectExtension.id, extension)

    project.plugins.apply("org.metaborg.gradle.config.java-library")

    configureConfigureLanguageProjectTask(project, extension)
    configureCompileTask(project, extension)
    configureCopySpoofaxLanguageTasks(project, extension)
  }

  private fun configureConfigureLanguageProjectTask(project: Project, extension: LanguageProjectExtension) {
    val configureTask = project.tasks.register("spoofaxConfigureLanguageProject") {
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
    project.tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(configureTask)
  }

  private fun configureCompileTask(project: Project, extension: LanguageProjectExtension) {
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

  private fun configureCopySpoofaxLanguageTasks(project: Project, extension: LanguageProjectExtension) {
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
