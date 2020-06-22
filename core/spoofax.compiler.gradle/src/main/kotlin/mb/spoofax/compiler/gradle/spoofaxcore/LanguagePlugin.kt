@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.common.util.Properties
import mb.resource.DefaultResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import java.io.IOException
import java.nio.charset.StandardCharsets

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
  internal val multilangAnalyzerCompiler = MultilangAnalyzerCompiler(templateCompiler)
  internal val languageProjectCompiler = LanguageProjectCompiler(templateCompiler, classloaderResourceService, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler, multilangAnalyzerCompiler)
  internal val adapterProjectCompiler = AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler, multilangAnalyzerCompiler)
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
  val multilangAnalyzer: MultilangAnalyzerCompiler.LanguageProjectInput.Builder? = null, // Optional

  val builder: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): LanguageProjectFinalized {
    // Attempt to load compiler properties from file.
    val spoofaxCompilerPropertiesFile = gradleProject.projectDir.resolve("spoofaxc.lock")
    val spoofaxCompilerProperties = Properties()
    if(spoofaxCompilerPropertiesFile.exists()) {
      spoofaxCompilerPropertiesFile.bufferedReader().use {
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
    val multilangAnalyzer = if(this.multilangAnalyzer != null) this.multilangAnalyzer.shared(shared).languageProject(languageProject).build() else null
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
    if(multilangAnalyzer != null) {
      builder.multilangAnalyzer(multilangAnalyzer)
    }
    val input = builder.build()

    // Save compiler properties to file.
    shared.savePersistentProperties(spoofaxCompilerProperties)
    spoofaxCompilerPropertiesFile.parentFile.mkdirs()
    spoofaxCompilerPropertiesFile.createNewFile()
    spoofaxCompilerPropertiesFile.bufferedWriter().use {
      try {
        spoofaxCompilerProperties.storeWithoutDate(it)
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

  init {
    settings.convention(LanguageProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxLanguageProject"
    private const val name = "Spoofax language project"
  }

  internal val finalized: LanguageProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
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
}

internal fun Project.whenLanguageProjectFinalized(closure: () -> Unit) = whenFinalized<LanguageProjectExtension>(closure)

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = LanguageProjectExtension(project)
    project.extensions.add(LanguageProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("org.metaborg.spoofax.gradle.base")

    project.afterEvaluate {
      configure(project, extension.finalized)
    }
  }

  private fun configure(project: Project, finalized: LanguageProjectFinalized) {
    configureProject(project, finalized)
    configureCompileTask(project, finalized)
    configureCopySpoofaxLanguageTasks(project, finalized)
  }

  private fun configureProject(project: Project, finalized: LanguageProjectFinalized) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(finalized.input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, finalized: LanguageProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.languageProject().project(), finalized.resourceService)
        finalized.compiler.compile(input)
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(project: Project, finalized: LanguageProjectFinalized) {
    val input = finalized.input
    val destinationPackage = input.languageProject().packagePath()
    val includeStrategoClasses = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
    val includeStrategoJavastratClasses = input.strategoRuntime().map { it.copyJavaStrategyClasses() }.orElse(false)
    val copyResources = finalized.compiler.getCopyResources(input)

    // Add language dependency.
    val languageDependency = input.languageSpecificationDependency().caseOf()
      .project<Dependency> { project.dependencies.project(it) }
      .module { project.dependencies.module(it.toGradleNotation()) }
      .files { project.dependencies.create(project.files(it)) }
    project.dependencies.add("compileLanguage", languageDependency)

    // Unpack the '.spoofax-language' archive.
    val languageFiles = project.configurations.getByName("languageFiles")
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      inputs.property("input", input)
      dependsOn(languageFiles)
      from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
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
}
