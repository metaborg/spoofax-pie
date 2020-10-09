@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofax3.plugin

import mb.common.message.KeyedMessages
import mb.common.message.Message
import mb.common.message.Messages
import mb.common.message.Severity
import mb.esv.spoofax.DaggerEsvComponent
import mb.libspoofax2.spoofax.DaggerLibSpoofax2Component
import mb.log.slf4j.SLF4JLoggerFactory
import mb.pie.runtime.PieBuilderImpl
import mb.resource.ResourceKey
import mb.resource.fs.FSPath
import mb.sdf3.spoofax.DaggerSdf3Component
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax3.*
import mb.spoofax.compiler.spoofax3.dagger.*
import mb.spoofax.compiler.spoofax3.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.platform.DaggerPlatformComponent
import mb.spoofax.core.platform.LoggerFactoryModule
import mb.spoofax.core.platform.PlatformPieModule
import mb.str.spoofax.DaggerStrategoComponent
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.nio.charset.StandardCharsets
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

    val platformComponent = DaggerPlatformComponent.builder()
      .loggerFactoryModule(LoggerFactoryModule(SLF4JLoggerFactory()))
      .platformPieModule(PlatformPieModule { PieBuilderImpl() })
      .build() // OPTO: cache instantiation of platform component?
    val component = DaggerSpoofax3CompilerGradleComponent.builder()
      .spoofax3CompilerModule(Spoofax3CompilerModule(TemplateCompiler(StandardCharsets.UTF_8)))
      .spoofax3CompilerGradleModule(Spoofax3CompilerGradleModule(languageProjectExtension.component.resourceService, languageProjectExtension.component.pie))
      // OPTO: cache instantiation of the language components?
      .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
      .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
      .esvComponent(DaggerEsvComponent.builder().platformComponent(platformComponent).build())
      .libSpoofax2Component(DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build())
      .build()

    val extension = Spoofax3LanguageProjectExtension(project)
    project.extensions.add(Spoofax3LanguageProjectExtension.id, extension)

    // Add a configuration closure to the language project that syncs our finalized input to their builder.
    languageProjectExtension.compilerInput { extension.compilerInputFinalized.syncTo(this) }

    project.afterEvaluate {
      configure(project, component, extension.compilerInputFinalized)
    }
  }

  private fun configure(
    project: Project,
    component: Spoofax3CompilerGradleComponent,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    configureProject(project, component, input)
    configureCompileTask(project, component, input)
  }

  private fun configureProject(
    project: Project,
    component: Spoofax3CompilerGradleComponent,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.spoofax3LanguageProject().generatedJavaSourcesDirectory(), component.resourceService)
    project.addMainResourceDirectory(input.spoofax3LanguageProject().generatedResourcesDirectory(), component.resourceService)
  }

  private fun configureCompileTask(
    project: Project,
    component: Spoofax3CompilerGradleComponent,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    val resourceService = component.resourceService
    val compileTask = project.tasks.register("compileSpoofax3BasedLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      input.parser().ifPresent { parserInput ->
        val sdf3RootDirectory = resourceService.toLocalFile(parserInput.sdf3RootDirectory())
        if(sdf3RootDirectory != null) {
          inputs.files(project.fileTree(sdf3RootDirectory) { include("**/*.sdf3") })
        } else {
          logger.warn("Cannot set SDF3 files as task inputs, because ${parserInput.sdf3RootDirectory()} cannot be converted into a local file. This disables incrementality for this Gradle task")
        }
        val sdf3ParseTableOutputFile = resourceService.toLocalFile(parserInput.sdf3ParseTableOutputFile())
        if(sdf3ParseTableOutputFile != null) {
          outputs.file(sdf3ParseTableOutputFile)
        } else {
          logger.warn("Cannot set the SDF3 parse table as a task output, because ${parserInput.sdf3ParseTableOutputFile()} cannot be converted into a local file. This disables incrementality for this Gradle task")
        }
      }
      input.strategoRuntime().ifPresent { strategoRuntimeInput ->
        val strategoRootDirectory = resourceService.toLocalFile(strategoRuntimeInput.strategoRootDirectory())
        if(strategoRootDirectory != null) {
          inputs.files(project.fileTree(strategoRootDirectory) { include("**/*.str") })
        } else {
          logger.warn("Cannot set Stratego files as task inputs, because ${strategoRuntimeInput.strategoRootDirectory()} cannot be converted into a local file. This disables incrementality for this Gradle task")
        }
        val strategoOutputDir = resourceService.toLocalFile(strategoRuntimeInput.strategoOutputDir())
        if(strategoOutputDir != null) {
          outputs.dir(strategoOutputDir)
        } else {
          logger.warn("Cannot set the Stratego output directory as a task output, because ${strategoRuntimeInput.strategoOutputDir()} cannot be converted into a local file. This disables incrementality for this Gradle task")
        }
      }

      doLast {
        component.pie.newSession().use { session ->
          val result = session.require(component.spoofax3LanguageProjectCompiler.createTask(input))
          result.ifOk {
            project.logMessages(it, FSPath(project.projectDir))
          }.ifErr { e ->
            e.caseOf().parserCompilerFail { parserCompilerException ->
              val message = "${e.message}. ${parserCompilerException.message}"
              parserCompilerException.caseOf().checkFail {
                logger.error(message)
                project.logMessages(it, FSPath(project.projectDir))
                throw GradleException(message)
              }.otherwise {
                val cause = parserCompilerException.cause
                if(cause != null) {
                  logger.error("$message. See cause at the end of the build")
                  throw GradleException(message, cause)
                } else {
                  logger.error(message)
                  throw GradleException(message)
                }
              }
            }.strategoRuntimeCompilerFail { strategoCompilerException ->
              val message = "${e.message}. ${strategoCompilerException.message}"
              strategoCompilerException.caseOf().checkFail {
                logger.error(message)
                project.logMessages(it, FSPath(project.projectDir))
                throw GradleException(message)
              }.otherwise {
                val cause = strategoCompilerException.cause
                if(cause != null) {
                  logger.error("$message. See cause at the end of the build")
                  throw GradleException(message, cause)
                } else {
                  logger.error(message)
                  throw GradleException(message)
                }
              }
            }
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
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
