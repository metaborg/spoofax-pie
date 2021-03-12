@file:Suppress("UnstableApiUsage")

package mb.spoofax.lwb.compiler.gradle

import mb.common.message.KeyedMessages
import mb.common.message.Message
import mb.common.message.Messages
import mb.common.message.Severity
import mb.common.util.ExceptionPrinter
import mb.log.dagger.DaggerLoggerComponent
import mb.log.dagger.LoggerModule
import mb.pie.api.Pie
import mb.pie.dagger.PieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.ResourceKey
import mb.resource.ResourceRuntimeException
import mb.resource.ResourceService
import mb.resource.dagger.DaggerRootResourceServiceComponent
import mb.resource.fs.FSPath
import mb.resource.hierarchical.ResourcePath
import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler
import mb.spoofax.lwb.compiler.CompileLanguage
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInput
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import java.util.*

open class LanguageExtension(project: Project) {
  companion object {
    internal const val id = "spoofaxLanguage"
  }
}

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // OPTO: cache components?
    val loggerComponent = DaggerLoggerComponent.builder()
      .loggerModule(LoggerModule.stdErrErrorsAndWarnings())
      .build()
    val resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
      .loggerComponent(loggerComponent)
      .build()
    val spoofax3Compiler = Spoofax3Compiler(
      loggerComponent,
      resourceServiceComponent.createChildModule(),
      PieModule { PieBuilderImpl() }
    )

    val extension = LanguageExtension(project)
    project.extensions.add(LanguageExtension.id, extension)

    val input = getInput(project, spoofax3Compiler);

    project.afterEvaluate {
      configure(project, spoofax3Compiler, input)
    }
  }

  private fun getInput(
    project: Project,
    spoofax3Compiler: Spoofax3Compiler
  ): CompileLanguageToJavaClassPathInput {
    spoofax3Compiler.pieComponent.pie.newSession().use {
      return it.require(spoofax3Compiler.cfgComponent.cfgRootDirectoryToObject.createTask(FSPath(project.projectDir)))
        .unwrap().compileLanguageToJavaClassPathInput // TODO: proper error handling
    }
  }

  private fun configure(
    project: Project,
    spoofax3Compiler: Spoofax3Compiler,
    input: CompileLanguageToJavaClassPathInput
  ) {
    val resourceService = spoofax3Compiler.resourceServiceComponent.resourceService
    val languageProjectCompiler = spoofax3Compiler.spoofaxCompilerComponent.languageProjectCompiler
    val compileLanguage = spoofax3Compiler.component.compileLanguage
    val adapterProjectCompiler = spoofax3Compiler.spoofaxCompilerComponent.adapterProjectCompiler
    val pie = spoofax3Compiler.pieComponent.pie
    configureProject(project, resourceService, languageProjectCompiler, adapterProjectCompiler, input)
    configureCompileLanguageProjectTask(project, resourceService, pie, languageProjectCompiler, input.languageProjectInput())
    configureCompileLanguageTask(project, resourceService, pie, compileLanguage, input.compileLanguageInput())
    configureCompileAdapterProjectTask(project, resourceService, pie, adapterProjectCompiler, input.adapterProjectInput())
  }

  private fun configureProject(
    project: Project,
    resourceService: ResourceService,
    languageProjectCompiler: LanguageProjectCompiler,
    adapterProjectCompiler: AdapterProjectCompiler,
    input: CompileLanguageToJavaClassPathInput
  ) {
    // Language project compiler
    val languageProjectInput = input.languageProjectInput()
    project.addMainJavaSourceDirectory(languageProjectInput.generatedJavaSourcesDirectory(), resourceService)
    languageProjectCompiler.getDependencies(languageProjectInput).forEach {
      it.addToDependencies(project)
    }
    // Language compiler
    val languageSpecificationInput = input.compileLanguageInput()
    project.addMainResourceDirectory(languageSpecificationInput.compileLanguageShared().generatedResourcesDirectory(), resourceService)
    project.addMainJavaSourceDirectory(languageSpecificationInput.compileLanguageShared().generatedJavaSourcesDirectory(), resourceService)
    // Adapter project compiler
    val adapterProjectInput = input.adapterProjectInput()
    project.addMainJavaSourceDirectory(adapterProjectInput.adapterProject().generatedJavaSourcesDirectory(), resourceService)
    adapterProjectCompiler.getDependencies(adapterProjectInput).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileLanguageProjectTask(
    project: Project,
    resourceService: ResourceService,
    pie: Pie,
    compiler: LanguageProjectCompiler,
    input: LanguageProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.javaSourceFiles().map { resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.languageProject().generatedJavaSourcesDirectory(), resourceService)
        synchronized(pie) {
          pie.newSession().use { session ->
            session.require(compiler.createTask(input))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCompileLanguageTask(
    project: Project,
    resourceService: ResourceService,
    pie: Pie,
    compiler: CompileLanguage,
    input: CompileLanguageInput
  ) {
    val compileTask = project.tasks.register("compileLanguage") {
      group = "spoofax compiler"
      inputs.property("input", input)

      // Inputs and outputs
      input.sdf3().ifPresent {
        // Input: all SDF3 files
        val rootDirectory = resourceService.toLocalFile(it.sourceDirectory())
        if(rootDirectory != null) {
          inputs.files(project.fileTree(rootDirectory) { include("**/*.sdf3") })
        } else {
          logger.warn("Cannot set SDF3 files as task inputs, because ${it.sourceDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }

        // Output: parse table file
        val outputFile = resourceService.toLocalFile(it.sdf3ParseTableOutputFile())
        if(outputFile != null) {
          outputs.file(outputFile)
        } else {
          logger.warn("Cannot set the SDF3 parse table as a task output, because ${it.sdf3ParseTableOutputFile()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }
      }
      input.esv().ifPresent {
        // Input: all ESV files
        val rootDirectory = resourceService.toLocalFile(it.esvRootDirectory())
        if(rootDirectory != null) {
          inputs.files(project.fileTree(rootDirectory) { include("**/*.esv") })
        } else {
          logger.warn("Cannot set ESV files as task inputs, because ${it.esvRootDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }

        // Output: ESV aterm format file
        val outputFile = resourceService.toLocalFile(it.esvAtermFormatOutputFile())
        if(outputFile != null) {
          outputs.file(outputFile)
        } else {
          logger.warn("Cannot set the ESV aterm format file as a task output, because ${it.esvAtermFormatOutputFile()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }
      }
      input.statix().ifPresent {
        // Input: all Statix files
        val rootDirectory = resourceService.toLocalFile(it.statixRootDirectory())
        if(rootDirectory != null) {
          inputs.files(project.fileTree(rootDirectory) { include("**/*.stx") })
        } else {
          logger.warn("Cannot set Statix files as task inputs, because ${it.statixRootDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }

        // Output: Statix output directory
        val outputDirectory = resourceService.toLocalFile(it.statixOutputDirectory())
        if(outputDirectory != null) {
          outputs.dir(outputDirectory)
        } else {
          logger.warn("Cannot set the Statix output directory as a task output, because ${it.statixOutputDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }
      }
      input.stratego().ifPresent {
        // Input: all Stratego files
        val rootDirectory = resourceService.toLocalFile(it.strategoRootDirectory())
        if(rootDirectory != null) {
          inputs.files(project.fileTree(rootDirectory) { include("**/*.str") })
        } else {
          logger.warn("Cannot set Stratego files as task inputs, because ${it.strategoRootDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }

        // Output: Stratego output directory
        val outputDirectory = resourceService.toLocalFile(it.strategoOutputDir())
        if(outputDirectory != null) {
          outputs.dir(outputDirectory)
        } else {
          logger.warn("Cannot set the Stratego output directory as a task output, because ${it.strategoOutputDir()} cannot be converted into a local file. This disables incrementality for this Gradle task")
        }
      }

      doLast {
        synchronized(pie) {
          pie.newSession().use { session ->
            val result = session.require(compiler.createTask(input))
            val projectDir = FSPath(project.projectDir)
            result.ifOk {
              project.logMessages(it, projectDir)
            }.ifErr {
              val exceptionPrinter = ExceptionPrinter()
              exceptionPrinter.addCurrentDirectoryContext(projectDir)
              project.logger.error(exceptionPrinter.printExceptionToString(it))
            }
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCompileAdapterProjectTask(
    project: Project,
    resourceService: ResourceService,
    pie: Pie,
    compiler: AdapterProjectCompiler,
    input: AdapterProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileAdapterProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.javaSourceFiles().map { resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.adapterProject().generatedJavaSourcesDirectory(), resourceService)
        synchronized(pie) {
          pie.newSession().use { session ->
            session.require(compiler.createTask(input))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}

fun Project.logMessages(messages: Messages, resource: ResourceKey?) {
  messages.forEach { message -> logMessage(message, resource) }
}

fun Project.logMessages(messages: KeyedMessages, backupResource: ResourceKey?) {
  logMessages(if(messages.resourceForMessagesWithoutKeys == null && backupResource != null) {
    messages.withResourceForMessagesWithoutKeys(backupResource)
  } else {
    messages
  })
}

fun Project.logMessages(allMessages: KeyedMessages) {
  allMessages.messagesWithKey.forEach { resource, messages ->
    messages.forEach {  message -> logMessage(message, resource) }
  }
  allMessages.messagesWithoutKey.forEach { message -> logMessage(message, allMessages.resourceForMessagesWithoutKeys) }
}

fun Project.logMessage(message: Message, resource: ResourceKey?) {
  val region = message.region
  val prefix = run {
    if(resource != null) {
      val optionalLine = if(region == null) OptionalInt.empty() else region.startLine
      val lineStr = if(optionalLine.isPresent) "${optionalLine.asInt + 1}:" else "" // + 1 because lines in most editors are not zero based.
      "$resource:$lineStr "
    } else {
      "(originating resource unknown) "
    }
  }
  val severity = message.severity
  val exception = message.exception
  val msg = "$prefix$severity: ${message.text}"
  @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
  when(exception) {
    null -> when(severity) {
      Severity.Trace -> logger.trace(msg)
      Severity.Debug -> logger.debug(msg)
      Severity.Info -> logger.info(msg)
      Severity.Warning -> logger.warn(msg)
      Severity.Error -> logger.error(msg)
    }
    else -> when(severity) {
      Severity.Trace -> logger.trace(msg, exception)
      Severity.Debug -> logger.debug(msg, exception)
      Severity.Info -> logger.info(msg, exception)
      Severity.Warning -> logger.warn(msg, exception)
      Severity.Error -> logger.error(msg, exception)
    }
  }
}

// Utilities copied from base LanguagePlugin.

fun Project.addMainJavaSourceDirectory(directory: ResourcePath, resourceService: ResourceService) {
  configure<SourceSetContainer> {
    named("main") {
      java {
        srcDir(resourceService.toLocalFile(directory)
          ?: throw GradleException("Cannot configure java sources directory, directory '$directory' is not on the local filesystem"))
      }
    }
  }
}

fun Project.addMainResourceDirectory(directory: ResourcePath, resourceService: ResourceService) {
  configure<SourceSetContainer> {
    named("main") {
      resources {
        srcDir(resourceService.toLocalFile(directory)
          ?: throw GradleException("Cannot configure resources directory, directory '$directory' is not on the local filesystem"))
      }
    }
  }
}

fun GradleConfiguredDependency.addToDependencies(project: Project): Dependency {
  val (configurationName, isPlatform) = caseOf()
    .api_("api" to false)
    .implementation_("implementation" to false)
    .compileOnly_("compileOnly" to false)
    .runtimeOnly_("runtimeOnly" to false)
    .testImplementation_("testImplementation" to false)
    .testCompileOnly_("testCompileOnly" to false)
    .testRuntimeOnly_("testRuntimeOnly" to false)
    .annotationProcessor_("annotationProcessor" to false)
    .testAnnotationProcessor_("testAnnotationProcessor" to false)
    .apiPlatform_("api" to true)
    .implementationPlatform_("implementation" to true)
    .annotationProcessorPlatform_("annotationProcessor" to true)
  var dependency = this.dependency.toGradleDependency(project)
  if(isPlatform) {
    dependency = project.dependencies.platform(dependency)
  }
  project.dependencies.add(configurationName, dependency)
  return dependency
}

fun GradleDependency.toGradleDependency(project: Project): Dependency {
  return caseOf()
    .project<Dependency> { project.dependencies.project(it) }
    .module { project.dependencies.create(it.groupId(), it.artifactId(), it.version().orElse(null)) }
    .files { project.dependencies.create(project.files(it)) }
}

fun Project.deleteDirectory(directory: ResourcePath, resourceService: ResourceService) {
  try {
    val genSourceDir = resourceService.getHierarchicalResource(directory)
    genSourceDir.delete(true)
  } catch(e: ResourceRuntimeException) {
    project.logger.warn("Failed to delete directory", e)
  }
}

