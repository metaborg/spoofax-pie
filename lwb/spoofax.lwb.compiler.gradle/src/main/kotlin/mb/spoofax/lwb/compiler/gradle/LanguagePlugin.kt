@file:Suppress("UnstableApiUsage")

package mb.spoofax.lwb.compiler.gradle

import mb.cfg.CompileLanguageInput
import mb.cfg.CompileLanguageSpecificationInput
import mb.cfg.task.CfgRootDirectoryToObjectException
import mb.common.message.KeyedMessages
import mb.common.message.Message
import mb.common.message.Messages
import mb.common.message.Severity
import mb.common.result.Result
import mb.common.util.ExceptionPrinter
import mb.log.dagger.DaggerLoggerComponent
import mb.log.dagger.LoggerModule
import mb.pie.api.Pie
import mb.pie.api.ValueSupplier
import mb.pie.dagger.PieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.ResourceKey
import mb.resource.ResourceService
import mb.resource.dagger.DaggerRootResourceServiceComponent
import mb.resource.dagger.ResourceServiceComponent
import mb.resource.fs.FSPath
import mb.resource.hierarchical.ResourcePath
import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.lwb.compiler.CheckLanguageSpecification
import mb.spoofax.lwb.compiler.CompileLanguageSpecification
import mb.spoofax.lwb.compiler.dagger.StandaloneSpoofax3Compiler
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoLibUtil
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import java.io.File
import java.util.*

open class LanguageExtension() {
  companion object {
    internal const val id = "spoofaxLanguage"
  }
}

open class Spoofax3AdapterExtension(project: Project, input: CompileLanguageInput) : AdapterProjectExtension(project) {
  companion object {
    internal const val id = "spoofax3LanguageAdapterProject"
  }

  override val languageProjectFinalized: Project? = project
  override val compilerInputFinalized: AdapterProjectCompiler.Input = input.adapterProjectInput()
}

open class Spoofax3LanguageExtension(project: Project, input: CompileLanguageInput) : LanguageProjectExtension(project) {
  companion object {
    internal const val id = "spoofax3LanguageProject"
  }

  override val sharedFinalized: Shared = input.shared()
  override val compilerInputFinalized = input.languageProjectInput()
  override val statixDependenciesFinalized: List<Project> = listOf()
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
    val spoofax3Compiler = StandaloneSpoofax3Compiler(
      loggerComponent,
      resourceServiceComponent.createChildModule(),
      PieModule { PieBuilderImpl() }
    )

    val extension = LanguageExtension()
    project.extensions.add(LanguageExtension.id, extension)

    try {
      val input = getInput(project, spoofax3Compiler)
      LanguagePluginInstance(project, resourceServiceComponent, spoofax3Compiler, input)
    } catch(e: CfgRootDirectoryToObjectException) {
      val exceptionPrinter = ExceptionPrinter()
      exceptionPrinter.addCurrentDirectoryContext(project.path)
      System.err.println("Reading configuration failed")
      exceptionPrinter.printException(e, System.err)
      throw e
    }
  }

  private fun getInput(project: Project, spoofax3Compiler: StandaloneSpoofax3Compiler): CompileLanguageInput {
    spoofax3Compiler.pieComponent.pie.newSession().use {
      return it.require(spoofax3Compiler.compiler.cfgComponent.cfgRootDirectoryToObject.createTask(FSPath(project.projectDir)))
        .unwrap().compileLanguageInput // Note: exception is caught in apply.
    }
  }
}

class LanguagePluginInstance(
  val project: Project,
  resourceServiceComponent: ResourceServiceComponent,
  val spoofax3Compiler: StandaloneSpoofax3Compiler,
  val compileLanguageInput: CompileLanguageInput
) {
  val resourceService: ResourceService = resourceServiceComponent.resourceService
  val pie: Pie = spoofax3Compiler.pieComponent.pie

  init {
    project.extensions.add(Spoofax3AdapterExtension.id, Spoofax3AdapterExtension(project, compileLanguageInput))
    project.extensions.add(Spoofax3LanguageExtension.id, Spoofax3LanguageExtension(project, compileLanguageInput))

    project.afterEvaluate {
      configure()
    }
  }


  private fun configure() {
    val languageProjectCompiler = spoofax3Compiler.compiler.spoofaxCompilerComponent.languageProjectCompiler
    val adapterProjectCompiler = spoofax3Compiler.compiler.spoofaxCompilerComponent.adapterProjectCompiler
    configureProject(languageProjectCompiler, spoofax3Compiler.compiler.component.spoofaxStrategoLibUtil, adapterProjectCompiler)
    configureCompileLanguageProjectTask(languageProjectCompiler, compileLanguageInput.languageProjectInput())
    val check = spoofax3Compiler.compiler.component.checkLanguageSpecification
    val compile = spoofax3Compiler.compiler.component.compileLanguageSpecification
    configureCompileLanguageTask(check, compile, compileLanguageInput.compileLanguageSpecificationInput())
    configureCompileAdapterProjectTask(adapterProjectCompiler, compileLanguageInput.adapterProjectInput())
  }

  private fun configureProject(
    languageProjectCompiler: LanguageProjectCompiler,
    spoofaxStrategoLibUtil: SpoofaxStrategoLibUtil,
    adapterProjectCompiler: AdapterProjectCompiler
  ) {
    // Language project compiler
    val languageProjectInput = compileLanguageInput.languageProjectInput()
    project.addMainJavaSourceDirectory(languageProjectInput.generatedJavaSourcesDirectory(), resourceService)
    languageProjectCompiler.getDependencies(languageProjectInput).forEach {
      it.addToDependencies(project)
    }
    // Language compiler
    val languageSpecificationInput = compileLanguageInput.compileLanguageSpecificationInput()
    project.addMainResourceDirectory(languageSpecificationInput.compileLanguageShared().generatedResourcesDirectory(), resourceService)
    project.addMainJavaSourceDirectory(languageSpecificationInput.compileLanguageShared().generatedJavaSourcesDirectory(), resourceService)
    project.dependencies.add("implementation", project.files(spoofaxStrategoLibUtil.strategoLibJavaClassPaths))
    // Adapter project compiler
    val adapterProjectInput = compileLanguageInput.adapterProjectInput()
    project.addMainJavaSourceDirectory(adapterProjectInput.adapterProject().generatedJavaSourcesDirectory(), resourceService)
    adapterProjectCompiler.getDependencies(adapterProjectInput).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileLanguageProjectTask(compiler: LanguageProjectCompiler, input: LanguageProjectCompiler.Input) {
    val compileTask = project.tasks.register("compileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.javaSourceFiles().map { resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.languageProject().generatedJavaSourcesDirectory(), resourceService)
        synchronized(pie) {
          pie.newSession().use { session ->
            session.require(compiler.createTask(ValueSupplier(Result.ofOk<LanguageProjectCompiler.Input, Exception>(input))))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCompileLanguageTask(
    check: CheckLanguageSpecification,
    compile: CompileLanguageSpecification,
    input: CompileLanguageSpecificationInput
  ) {
    val compileTask = project.tasks.register("compileLanguage") {
      group = "spoofax compiler"
      inputs.property("input", input)

      // Inputs and outputs
      input.sdf3().ifPresent { sdf3Config ->
        sdf3Config.source().caseOf()
          .files { mainSourceDirectory, _ ->
            // Input: all SDF3 files in the main source directory
            mainSourceDirectory.tryAsLocal("SDF3 files in main source directory") { dir ->
              inputs.files(project.fileTree(dir) { include("**/*.sdf3") })
            }
          }
          .prebuilt { inputParseTableAtermFile, inputParseTablePersistedFile ->
            // Input: prebuilt input files
            inputParseTableAtermFile.tryAsLocal("SDF3 prebuilt parse table ATerm file") { file ->
              inputs.file(file)
            }
            inputParseTablePersistedFile.tryAsLocal("SDF3 prebuilt parse table persisted file") { file ->
              inputs.file(file)
            }
          }
        // Output: output files
        sdf3Config.parseTableAtermOutputFile().tryAsLocal("SDF3 parse table ATerm output file") { file ->
          outputs.file(file)
        }
        sdf3Config.parseTablePersistedOutputFile().tryAsLocal("SDF3 parse table persisted output file") { file ->
          outputs.file(file)
        }
      }
      input.esv().ifPresent { esvConfig ->
        esvConfig.source().caseOf()
          .files { mainSourceDirectory, _, includeDirectories, includeLibSpoofax2Exports, libSpoofax2UnarchiveDirectory ->
            // Input: all ESV files in the main source directory and include directories
            mainSourceDirectory.tryAsLocal("ESV files in main source directory") { dir ->
              inputs.files(project.fileTree(dir) { include("**/*.esv") })
            }
            includeDirectories.forEach { includeDirectory ->
              includeDirectory.tryAsLocal("ESV files in include directory") { dir ->
                inputs.files(project.fileTree(dir) { include("**/*.esv") })
              }
            }
            if(includeLibSpoofax2Exports) {
              // Output: libspoofax2 unarchive directory
              libSpoofax2UnarchiveDirectory.tryAsLocal("libspoofax2 unarchive directory") { dir ->
                outputs.dir(dir)
              }
            }
          }
          .prebuilt { inputFile ->
            // Input: prebuilt input file
            inputFile.tryAsLocal("ESV prebuilt file") { file ->
              inputs.file(file)
            }
          }

        // Output: output file
        esvConfig.outputFile().tryAsLocal("ESV output file") { file ->
          outputs.file(file)
        }
      }
      input.statix().ifPresent { statixConfig ->
        statixConfig.source().caseOf()
          .files { mainSourceDirectory, _, includeDirectories ->
            // Input: all Statix files in the main source directory and include directories
            mainSourceDirectory.tryAsLocal("Statix files in main source directory") { dir ->
              inputs.files(project.fileTree(dir) {
                include("**/*.stx")
                include("**/*.stxtest")
              })
            }
            includeDirectories.forEach { includeDirectory ->
              includeDirectory.tryAsLocal("Statix files in include directory") { dir ->
                inputs.files(project.fileTree(dir) {
                  include("**/*.stx")
                  include("**/*.stxtest")
                })
              }
            }
          }
          .prebuilt { specAtermDirectory ->
            // Input: prebuilt spec ATerm directory
            specAtermDirectory.tryAsLocal("Statix prebuilt spec ATerm directory") { dir ->
              inputs.files(project.fileTree(dir) {
                include("**/*.aterm")
              })
            }
          }

        // Output: output spec ATerm directory
        statixConfig.outputSpecAtermDirectory().tryAsLocal("Statix output spec ATerms directory") { dir ->
          outputs.files(project.fileTree(dir) {
            include("**/*.aterm")
          })
        }
      }
      input.stratego().ifPresent { strategoConfig ->
        // Input: all Stratego files in the main source directory and include directories
        strategoConfig.source().files.mainSourceDirectory().tryAsLocal("Stratego files in main source directory") { dir ->
          inputs.files(project.fileTree(dir) {
            include("**/*.str")
            include("**/*.str2")
          })
        }
        strategoConfig.source().files.includeDirectories().forEach { includeDirectory ->
          includeDirectory.tryAsLocal("Stratego files in include directory") { dir ->
            inputs.files(project.fileTree(dir) {
              include("**/*.str")
              include("**/*.str2")
            })
          }
        }

        // Output: output Java sources directory
        strategoConfig.javaSourceFileOutputDirectory().tryAsLocal("Stratego output Java sources directory") { dir ->
          outputs.files(project.fileTree(dir) {
            include("**/*.java")
          })
        }
      }

      doLast {
        synchronized(pie) {
          pie.newSession().use { session ->
            val rootDirectory = FSPath(project.projectDir)

            val messages = session.require(check.createTask(rootDirectory))
            if(messages.containsError()) {
              val exceptionPrinter = ExceptionPrinter()
              exceptionPrinter.addCurrentDirectoryContext(rootDirectory)
              project.logger.error(exceptionPrinter.printMessagesToString(messages.filter { it.isError }))
              throw GradleException("Checking language produced errors")
            }

            val compileResult = session.require(compile.createTask(rootDirectory))
            compileResult.ifOk {
              // HACK: do not log messages for now, as Stratego 2 generates a lot of warnings.
              //project.logMessages(it.messages(), rootDirectory)
            }.ifErr {
              val exceptionPrinter = ExceptionPrinter()
              exceptionPrinter.addCurrentDirectoryContext(rootDirectory)
              project.logger.error(exceptionPrinter.printExceptionToString(it))
              throw GradleException("Compiling language produced errors")
            }
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun ResourcePath.tryAsLocal(name: String, func: (file: File) -> Unit) {
    val local = resourceService.toLocalFile(this)
    if(local != null) {
      func(local)
    } else {
      project.logger.warn("Cannot set the $name as a task dependency, because '${this}' cannot be converted into a local file. This disables incrementality for this Gradle task")
    }
  }

  private fun configureCompileAdapterProjectTask(
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
            session.require(compiler.createTask(ValueSupplier(Result.ofOk<AdapterProjectCompiler.Input, Exception>(input))))
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
  allMessages.messagesWithKey.forEachEntry { resource, messages ->
    messages.forEach { message -> logMessage(message, resource) }
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
