import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.EnclosingCommandContextType
import mb.spoofax.core.language.command.CommandExecutionType

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"
dependencies {
  api(compositeBuild("spt.api"))

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

languageProject {
  shared {
    name("SPT")
    defaultClassPrefix("Spt")
    defaultPackageId("mb.spt")
  }
  compilerInput {
    withParser().run {
      startSymbol("TestSuite")
    }
    withStyler()
    withStrategoRuntime()
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.spt:${ext["spoofax2DevenvVersion"]}"))
  }
}

val packageId = "mb.spt"
val taskPackageId = "$packageId.task"
val commandPackageId = "$packageId.command"

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  // Extend resources component and add modules
  baseResourcesComponent(packageId, "BaseSptResourcesComponent")
  extendResourcesComponent(packageId, "SptResourcesComponent")
  addAdditionalResourcesModules("$packageId.resource", "SptTestCaseResourceModule")

  // Extend component and add modules
  baseComponent(packageId, "BaseSptComponent")
  extendComponent(packageId, "SptComponent")
  addAdditionalModules("$packageId.fromterm", "ExpectationFromTermsModule")

  // Wrap Check and rename base tasks
  isMultiFile(false)
  baseCheckTaskDef(taskPackageId, "BaseSptCheck")
  baseCheckMultiTaskDef(taskPackageId, "BaseSptCheckMulti")
  extendCheckTaskDef(taskPackageId, "SptCheck")

  // Internal task definitions
  val check = TypeInfo.of(taskPackageId, "SptCheck")
  addTaskDefs(check)
  val checkForOutput = TypeInfo.of(taskPackageId, "SptCheckForOutput")
  val checkForOutputAggregator = TypeInfo.of(taskPackageId, "SptCheckForOutputAggregator")
  addTaskDefs(checkForOutput, checkForOutputAggregator)

  // Show (debugging) task definitions
  val debugTaskPackageId = "$taskPackageId.debug"
  val showParsedAst = TypeInfo.of(debugTaskPackageId, "SptShowParsedAst")
  val showParsedTokens = TypeInfo.of(debugTaskPackageId, "SptShowParsedTokens")
  addTaskDefs(showParsedAst, showParsedTokens)
  // Show (debugging) commands
  fun showCommand(taskDefType: TypeInfo, argType: TypeInfo, resultName: String) = CommandDefRepr.builder()
    .type(commandPackageId, taskDefType.id() + "Command")
    .taskDefType(taskDefType)
    .argType(argType)
    .displayName("Show $resultName")
    .description("Shows the $resultName of the file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()

  val showParsedAstCommand = showCommand(showParsedAst, TypeInfo.of("mb.jsglr.pie", "ShowParsedAstTaskDef.Args"), "parsed AST")
  val showParsedTokensCommand = showCommand(showParsedTokens, TypeInfo.of("mb.jsglr.pie", "ShowParsedTokensTaskDef.Args"), "parsed tokens")
  val showCommands = listOf(
    showParsedAstCommand,
    showParsedTokensCommand
  )
  addAllCommandDefs(showCommands)

  // Add test running tasks
  val runTestSuite = TypeInfo.of(taskPackageId, "SptRunTestSuite")
  val runTestSuites = TypeInfo.of(taskPackageId, "SptRunTestSuites")
  addTaskDefs(runTestSuite, runTestSuites)

  // Add test running commands
  val runTestSuiteCommand = CommandDefRepr.builder()
    .type(commandPackageId, runTestSuite.id() + "Command")
    .taskDefType(runTestSuite)
    .argType(TypeInfo.of(taskPackageId, "SptRunTestSuite.Args"))
    .displayName("Run SPT tests")
    .description("Run the SPT tests in this file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
    .addAllParams(listOf(
      ParamRepr.of(
        "rootDir", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
        true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
      ),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  val runTestSuitesCommand = CommandDefRepr.builder()
    .type(commandPackageId, runTestSuites.id() + "Command")
    .taskDefType(runTestSuites)
    .argType(TypeInfo.of(taskPackageId, "SptRunTestSuites.Args"))
    .displayName("Run SPT tests")
    .description("Run the SPT tests in this directory")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
    .addAllParams(listOf(
      ParamRepr.of(
        "rootDir",
        TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
        true,
        ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
      ),
      ParamRepr.of("directory",
        TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
        true,
        ArgProviderRepr.context(CommandContextType.Directory))
    ))
    .build()
  addCommandDefs(runTestSuiteCommand, runTestSuitesCommand)

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Debug",
      showCommands.flatMap {
        listOf(
          CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem(),
          CommandActionRepr.builder().manualContinuous(it).fileRequired().buildItem()
        )
      }
    ),
    CommandActionRepr.builder().manualOnce(runTestSuiteCommand).fileRequired().enclosingProjectRequired().buildItem()
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Debug",
      showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem()) }
    ),
    CommandActionRepr.builder().manualOnce(runTestSuiteCommand).fileRequired().enclosingProjectRequired().buildItem(),
    CommandActionRepr.builder().manualOnce(runTestSuitesCommand).directoryRequired().enclosingProjectRequired().buildItem()
  )
}
