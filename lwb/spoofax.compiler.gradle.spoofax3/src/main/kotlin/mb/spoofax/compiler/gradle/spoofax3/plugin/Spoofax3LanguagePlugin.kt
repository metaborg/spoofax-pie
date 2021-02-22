@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofax3.plugin

import mb.common.message.KeyedMessages
import mb.common.message.Message
import mb.common.message.Messages
import mb.common.message.Severity
import mb.resource.ResourceKey
import mb.resource.fs.FSPath
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.spoofax3.dagger.*
import mb.spoofax.compiler.spoofax3.language.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.util.*

open class Spoofax3LanguageProjectExtension(project: Project) {
  val compilerInput: Property<Spoofax3LanguageProjectCompilerInputBuilder> = project.objects.property()
  val spoofax3LanguageProject: Property<Spoofax3LanguageProject.Builder> = project.objects.property()

  fun compilerInput(closure: Spoofax3LanguageProjectCompilerInputBuilder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(Spoofax3LanguageProjectCompilerInputBuilder())
    spoofax3LanguageProject.convention(Spoofax3LanguageProject.builder())
  }

  companion object {
    internal const val id = "spoofax3BasedLanguageProject"
    private const val name = "Spoofax3-based language project"
  }

  internal val spoofax3LanguageProjectFinalized: Spoofax3LanguageProject by lazy {
    project.logger.debug("Finalizing $name's project in $project")
    spoofax3LanguageProject.finalizeValue()
    val languageProject = project.extensions.getByType<LanguageProjectExtension>().languageProjectFinalized
    spoofax3LanguageProject.get()
      .languageProject(languageProject)
      .build()
  }

  val compilerInputFinalized: Spoofax3LanguageProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = project.extensions.getByType<LanguageProjectExtension>().sharedFinalized

    val properties = project.loadLockFileProperties()
    val input = compilerInput.get().build(properties, shared, spoofax3LanguageProjectFinalized)
    input.savePersistentProperties(properties)
    project.saveLockFileProperties(properties)

    input
  }
}

@Suppress("unused")
open class Spoofax3LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // First apply the language plugin to make its extension available.
    project.plugins.apply("org.metaborg.spoofax.compiler.gradle.language")
    val languageProjectExtension = project.extensions.getByType<LanguageProjectExtension>()

    // OPTO: cache instantiation components.
    val components = languageProjectExtension.components
    val spoofax3Compiler = Spoofax3Compiler(
      components.loggerComponent,
      components.resourceServiceComponent.createChildModule(),
      components.pieComponent.createChildModule()
    )

    val extension = Spoofax3LanguageProjectExtension(project)
    project.extensions.add(Spoofax3LanguageProjectExtension.id, extension)

    // Add a configuration closure to the language project that syncs our finalized input to their builder.
    languageProjectExtension.compilerInput { extension.compilerInputFinalized.syncTo(this) }

    project.afterEvaluate {
      configure(project, spoofax3Compiler, extension.compilerInputFinalized)
    }
  }

  private fun configure(
    project: Project,
    spoofax3Compiler: Spoofax3Compiler,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    configureProject(project, spoofax3Compiler, input)
    configureCompileTask(project, spoofax3Compiler, input)
  }

  private fun configureProject(
    project: Project,
    spoofax3Compiler: Spoofax3Compiler,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    val resourceService = spoofax3Compiler.resourceServiceComponent.resourceService
    project.addMainJavaSourceDirectory(input.spoofax3LanguageProject().generatedJavaSourcesDirectory(), resourceService)
    project.addMainResourceDirectory(input.spoofax3LanguageProject().generatedResourcesDirectory(), resourceService)
  }

  private fun configureCompileTask(
    project: Project,
    spoofax3Compiler: Spoofax3Compiler,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    val resourceService = spoofax3Compiler.resourceServiceComponent.resourceService
    val compileTask = project.tasks.register("compileSpoofax3BasedLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)

      // Inputs and outputs
      input.parser().ifPresent {
        // Input: all SDF3 files
        val rootDirectory = resourceService.toLocalFile(it.sdf3RootDirectory())
        if(rootDirectory != null) {
          inputs.files(project.fileTree(rootDirectory) { include("**/*.sdf3") })
        } else {
          logger.warn("Cannot set SDF3 files as task inputs, because ${it.sdf3RootDirectory()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }

        // Output: parse table file
        val outputFile = resourceService.toLocalFile(it.sdf3ParseTableOutputFile())
        if(outputFile != null) {
          outputs.file(outputFile)
        } else {
          logger.warn("Cannot set the SDF3 parse table as a task output, because ${it.sdf3ParseTableOutputFile()} cannot be converted into a local file. This breaks incrementality for this Gradle task")
        }
      }
      input.styler().ifPresent {
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
      input.constraintAnalyzer().ifPresent {
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
      input.strategoRuntime().ifPresent {
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
        spoofax3Compiler.pieComponent.pie.newSession().use { session ->
          val result = session.require(spoofax3Compiler.component.spoofax3LanguageProjectCompiler.createTask(input))
          result.ifOk {
            project.logMessages(it, FSPath(project.projectDir))
          }.ifErr {
            project.handleError(it)
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}

fun Project.handleError(err: CompilerException) {
  val message = "${err.message}. ${err.subMessage}"
  err.subMessages.ifPresent {
    logger.error(message)
    project.logMessages(it, FSPath(projectDir))
    throw GradleException(message)
  }
  if(err.subCause != null) {
    logger.error("$message. See cause at the end of the build")
    throw GradleException(message, err.subCause)
  }
  logger.error(message)
  throw GradleException(message)
}

fun Project.logMessages(messages: KeyedMessages, backupResource: ResourceKey) {
  messages.messagesWithKey.forEach { (resource, messages) ->
    logMessages(messages, resource, backupResource)
  }
  messages.messagesWithoutKey.forEach { message ->
    logMessage(message, null, backupResource)
  }
}

fun Project.logMessages(messages: Messages, resource: ResourceKey?, backupResource: ResourceKey) {
  messages.forEach { message -> logMessage(message, resource, backupResource) }
}

fun Project.logMessages(messages: Iterable<Message>, resource: ResourceKey?, backupResource: ResourceKey) {
  messages.forEach { message -> logMessage(message, resource, backupResource) }
}

fun Project.logMessage(message: Message, resource: ResourceKey?, backupResource: ResourceKey) {
  val region = message.region
  val prefix = run {
    if(resource != null) {
      val optionalLine = if(region == null) OptionalInt.empty() else region.startLine
      val lineStr = if(optionalLine.isPresent) "${optionalLine.asInt + 1}:" else "" // + 1 because lines in most editors are not zero based.
      "$resource:$lineStr "
    } else {
      "in $backupResource (originating resource unknown) "
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
