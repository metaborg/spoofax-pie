@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofax3.plugin

import mb.common.message.KeyedMessages
import mb.common.message.Severity
import mb.log.noop.NoopLoggerFactory
import mb.pie.runtime.PieBuilderImpl
import mb.sdf3.spoofax.DaggerSdf3Component
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax3.*
import mb.spoofax.compiler.spoofax3.language.*
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

open class Spoofax3LanguageProjectExtension(project: Project) {
  val compilerInput: Property<Spoofax3LanguageProjectCompilerInputBuilder> = project.objects.property()

  fun compilerInput(closure: Spoofax3LanguageProjectCompilerInputBuilder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(Spoofax3LanguageProjectCompilerInputBuilder())
  }

  companion object {
    internal const val id = "spoofax3BasedLanguageProject"
    private const val name = "Spoofax3-based language project"
  }

  val compilerInputFinalized: Spoofax3LanguageProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    compilerInput.get().build(project.extensions.getByType<LanguageProjectExtension>().languageProjectFinalized)
  }
}

@Suppress("unused")
open class Spoofax3LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // First apply the language plugin to make its extension available.
    project.plugins.apply("org.metaborg.spoofax.compiler.gradle.language")
    val languageProjectExtension = project.extensions.getByType<LanguageProjectExtension>()

    val platformComponent = DaggerPlatformComponent.builder()
      .loggerFactoryModule(LoggerFactoryModule(NoopLoggerFactory()))
      .platformPieModule(PlatformPieModule { PieBuilderImpl() })
      .build() // OPTO: cache instantiation of platform component?
    val component = DaggerSpoofax3CompilerGradleComponent.builder()
      .spoofax3CompilerGradleModule(Spoofax3CompilerGradleModule(languageProjectExtension.component.resourceService, languageProjectExtension.component.pie))
      // OPTO: cache instantiation of the SDF3 and Stratego components?
      .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
      .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
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
    project.addMainJavaSourceDirectory(input.generatedJavaSourcesDirectory(), component.resourceService)
    project.addMainResourceDirectory(input.generatedResourcesDirectory(), component.resourceService)
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
            project.logMessages(it)
          }.ifErr { e ->
            e.caseOf().parserCompilerFail { parserCompilerException ->
              val message = "${e.message}. ${parserCompilerException.message}"
              parserCompilerException.caseOf().checkFail {
                logger.error(message)
                project.logMessages(it)
                throw GradleException(message)
              }.createParseTableFail {
                logger.error(message, it)
                throw GradleException(message, it)
              }.otherwise {
                logger.error(message)
                throw GradleException(message)
              }
            }.strategoRuntimeCompilerFail {
              val message = e.message
              logger.error(message, it)
              throw GradleException(message, it)
            }
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}

fun Project.logMessages(messages: KeyedMessages) {
  messages.messagesWithKey.forEach { (resource, messages) ->
    messages.forEach { message ->
      val region = message.region
      val severity = message.severity
      val exception = message.exception
      val msg = "$resource:${if(region != null) "${region.startOffset}:" else ""} $severity: ${message.text}"
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
  }
}
