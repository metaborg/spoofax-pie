import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  api(project(":spt.api"))

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
    withStrategoRuntime().run {
      addStrategyPackageIds("org.metaborg.meta.lang.spt.trans")
      addInteropRegisterersByReflection("org.metaborg.meta.lang.spt.trans.InteropRegisterer")
    }
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
  // Extend component
  baseComponent(packageId, "BaseSptComponent")
  extendComponent(packageId, "SptComponent")
  // Add modules
  addAdditionalModules(packageId, "SptExpectationFromTermsModule");

  // Wrap Check and rename base tasks
  isMultiFile(false)
  baseCheckTaskDef(taskPackageId, "BaseSptCheck")
  baseCheckMultiTaskDef(taskPackageId, "BaseSptCheckMulti")
  extendCheckTaskDef(taskPackageId, "SptCheck")

  // Internal task definitions
  val check = TypeInfo.of(taskPackageId, "SptCheck")
  addTaskDefs(check)

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

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Debug",
      showCommands.flatMap {
        listOf(
          CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem(),
          CommandActionRepr.builder().manualContinuous(it).fileRequired().buildItem()
        )
      }
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Debug",
      showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem()) }
    )
  )
}
