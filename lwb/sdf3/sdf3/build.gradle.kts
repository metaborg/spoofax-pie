import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  api("org.metaborg:sdf2parenthesize")
  api("org.metaborg:statix.solver")
  api("org.metaborg:statix.common")
  api("org.metaborg:statix.multilang")

  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("SDF3")
    defaultClassPrefix("Sdf3")
    defaultPackageId("mb.sdf3")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withConstraintAnalyzer().run {
      strategoStrategy("statix-editor-analyze")
      enableNaBL2(false)
      enableStatix(true)
      multiFile(true)
    }
    withStrategoRuntime().run {
      addInteropRegisterersByReflection("org.metaborg.meta.lang.template.strategies.InteropRegisterer")
      extendStrategoRuntimeBuilderFactory("mb.sdf3", "Sdf3ManualStrategoRuntimeBuilderFactory")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer().run {
      copyStatix(true)
    }
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(true)
    }
    project.run {
      addAdditionalCopyResources("target/metaborg/EditorService-pretty.pp.af")

      // Use group ID "org.metaborg.bootstraphack" when building as part of devenv (not standalone).
      val spoofax2GroupId = if(gradle.parent?.rootProject?.name == "spoofax3.root") "org.metaborg" else "org.metaborg.bootstraphack"
      val spoofax2Version = System.getProperty("spoofax2Version")
      languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:org.metaborg.meta.lang.template:$spoofax2Version"))
    }
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    withConstraintAnalyzer()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.sdf3"
  val taskPackageId = "$packageId.task"
  val commandPackageId = "$packageId.command"


  // Utility task definitions
  val desugar = TypeInfo.of(taskPackageId, "Sdf3Desugar")
  val createSpec = TypeInfo.of(taskPackageId, "Sdf3CreateSpec")
  val prettyPrint = TypeInfo.of(taskPackageId, "Sdf3PrettyPrint")
  addTaskDefs(desugar, createSpec, prettyPrint)


  // Generation task definitions
  val toCompletionColorer = TypeInfo.of(taskPackageId, "Sdf3ToCompletionColorer")
  val toCompletionRuntime = TypeInfo.of(taskPackageId, "Sdf3ToCompletionRuntime")
  val toCompletion = TypeInfo.of(taskPackageId, "Sdf3ToCompletion")
  val toSignature = TypeInfo.of(taskPackageId, "Sdf3ToSignature")
  val toDynsemSignature = TypeInfo.of(taskPackageId, "Sdf3ToDynsemSignature")
  val toPrettyPrinter = TypeInfo.of(taskPackageId, "Sdf3ToPrettyPrinter")
  val toPermissive = TypeInfo.of(taskPackageId, "Sdf3ToPermissive")
  val toNormalForm = TypeInfo.of(taskPackageId, "Sdf3ToNormalForm")
  val specToParseTable = TypeInfo.of(taskPackageId, "Sdf3SpecToParseTable")
  val parseTableToParenthesizer = TypeInfo.of(taskPackageId, "Sdf3ParseTableToParenthesizer")
  val parseTableToFile = TypeInfo.of(taskPackageId, "Sdf3ParseTableToFile")
  val preStatix = TypeInfo.of(taskPackageId, "Sdf3PreStatix")
  val postStatix = TypeInfo.of(taskPackageId, "Sdf3PostStatix")
  val indexAst = TypeInfo.of(taskPackageId, "Sdf3IndexAst")
  addTaskDefs(toCompletionColorer, toCompletionRuntime, toCompletion, toSignature, toDynsemSignature,
    toPrettyPrinter, toPermissive, toNormalForm, specToParseTable, parseTableToParenthesizer, parseTableToFile,
    preStatix, postStatix, indexAst)


  // Show (debugging) task definitions
  val debugTaskPackageId = "$taskPackageId.debug"
  val showDesugar = TypeInfo.of(debugTaskPackageId, "Sdf3ShowDesugar")
  val showPermissive = TypeInfo.of(debugTaskPackageId, "Sdf3ShowPermissive")
  val showNormalForm = TypeInfo.of(debugTaskPackageId, "Sdf3ShowNormalForm")
  val showSignature = TypeInfo.of(debugTaskPackageId, "Sdf3ShowSignature")
  val showDynsemSignature = TypeInfo.of(debugTaskPackageId, "Sdf3ShowDynsemSignature")
  val showCompletion = TypeInfo.of(debugTaskPackageId, "Sdf3ShowCompletion")
  val showCompletionRuntime = TypeInfo.of(debugTaskPackageId, "Sdf3ShowCompletionRuntime")
  val showCompletionColorer = TypeInfo.of(debugTaskPackageId, "Sdf3ShowCompletionColorer")
  val showSpecParseTable = TypeInfo.of(debugTaskPackageId, "Sdf3ShowSpecParseTable")
  val showSpecParenthesizer = TypeInfo.of(debugTaskPackageId, "Sdf3ShowSpecParenthesizer")
  addTaskDefs(
    showDesugar,
    showPermissive,
    showNormalForm,
    showSignature,
    showDynsemSignature,
    showCompletion,
    showCompletionRuntime,
    showCompletionColorer,
    showSpecParseTable,
    showSpecParenthesizer
  )


  // Show (debugging) commands
  val showAbstractTaskDef = TypeInfo.of(debugTaskPackageId, "ShowTaskDef")
  val showAnalyzedAbstractTaskDef = TypeInfo.of(debugTaskPackageId, "ShowAnalyzedTaskDef")
  fun showCommand(taskDefType: TypeInfo, resultName: String) = CommandDefRepr.builder()
    .type(commandPackageId, taskDefType.id() + "Command")
    .taskDefType(taskDefType)
    .argType(showAbstractTaskDef.appendToId(".Args"))
    .displayName("Show $resultName")
    .description("Shows the $resultName of the file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
    ))
    .build()

  fun showAnalyzedCommand(taskDefType: TypeInfo, resultName: String) = CommandDefRepr.builder()
    .type(commandPackageId, taskDefType.id() + "Command")
    .taskDefType(taskDefType)
    .argType(showAnalyzedAbstractTaskDef.appendToId(".Args"))
    .displayName("Show $resultName")
    .description("Shows the $resultName of the file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("project", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
    ))
    .build()

  val showDesugarCommand = showCommand(showDesugar, "desugared")
  val showPermissiveCommand = showCommand(showPermissive, "permissive grammar")
  val showNormalFormCommand = showCommand(showNormalForm, "normal-form")
  val showSignatureCommand = showAnalyzedCommand(showSignature, "Stratego signatures")
  val showDynsemSignatureCommand = showAnalyzedCommand(showDynsemSignature, "DynSem signatures")

  val showCompletionCommand = showCommand(showCompletion, "completion insertions")
  val showCompletionRuntimeCommand = showCommand(showCompletionRuntime, "completion runtime")
  val showCompletionColorerCommand = showCommand(showCompletionColorer, "completion colorer")
  val showSpecParenthesizerCommand = CommandDefRepr.builder()
    .type(commandPackageId, showSpecParenthesizer.id() + "Command")
    .taskDefType(showSpecParenthesizer)
    .argType(showSpecParenthesizer.appendToId(".Args"))
    .displayName("Show parenthesizer")
    .description("Shows the parenthesizer built from given main file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("project", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
    ))
    .build()

  val showCommands = listOf(
    showDesugarCommand,
    showPermissiveCommand,
    showNormalFormCommand,
    showCompletionCommand,
    showCompletionRuntimeCommand,
    showCompletionColorerCommand
  )
  addAllCommandDefs(showCommands)
  val showAnalyzedCommands = listOf(
    showSignatureCommand,
    showDynsemSignatureCommand,
    showSpecParenthesizerCommand
  )
  addAllCommandDefs(showAnalyzedCommands)


  // Additional show (debugging) commands that do not fit the regular pattern.
  val showSpecParseTableCommand = CommandDefRepr.builder()
    .type(commandPackageId, showSpecParseTable.id() + "Command")
    .taskDefType(showSpecParseTable)
    .argType(showSpecParseTable.appendToId(".Args"))
    .displayName("Show parse table")
    .description("Shows the parse table built from given main file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("project", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  addCommandDefs(
    showSpecParseTableCommand,
    showSpecParenthesizerCommand
  )
  val showSpecParseTableOnceMenuItem = CommandActionRepr.builder().manualOnce(showSpecParseTableCommand).fileRequired().enclosingProjectRequired().buildItem()
  val showSpecParseTableContinuousMenuItem = CommandActionRepr.builder().manualContinuous(showSpecParseTableCommand).fileRequired().enclosingProjectRequired().buildItem()
  val showSpecParseTableMenuItems = listOf(showSpecParseTableOnceMenuItem, showSpecParseTableContinuousMenuItem)


  // Show (debugging) menu command actions
  fun showManualOnce(commandDef: CommandDefRepr, concrete: Boolean) = CommandActionRepr.builder().manualOnce(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().buildItem()
  fun showManualContinuous(commandDef: CommandDefRepr, concrete: Boolean) = CommandActionRepr.builder().manualContinuous(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().buildItem()
  fun showAnalyzedManualOnce(commandDef: CommandDefRepr, concrete: Boolean) = CommandActionRepr.builder().manualOnce(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().enclosingProjectRequired().buildItem()
  fun showAnalyzedManualContinuous(commandDef: CommandDefRepr, concrete: Boolean) = CommandActionRepr.builder().manualContinuous(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().enclosingProjectRequired().buildItem()
  val showAbstractEditorMenuItems = showCommands.flatMap { listOf(showManualOnce(it, false), showManualContinuous(it, false)) }
  val showConcreteEditorMenuItems = showCommands.flatMap { listOf(showManualOnce(it, true), showManualContinuous(it, true)) }
  val showAbstractResourceMenuItems = showCommands.map { showManualOnce(it, false) }
  val showConcreteResourceMenuItems = showCommands.map { showManualOnce(it, true) }
  val showAnalyzedAbstractEditorMenuItems = showAnalyzedCommands.flatMap { listOf(showAnalyzedManualOnce(it, false), showAnalyzedManualContinuous(it, false)) }
  val showAnalyzedConcreteEditorMenuItems = showAnalyzedCommands.flatMap { listOf(showAnalyzedManualOnce(it, true), showAnalyzedManualContinuous(it, true)) }
  val showAnalyzedAbstractResourceMenuItems = showAnalyzedCommands.map { showAnalyzedManualOnce(it, false) }
  val showAnalyzedConcreteResourceMenuItems = showAnalyzedCommands.map { showAnalyzedManualOnce(it, true) }


  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Transform",
        MenuItemRepr.menu("Abstract", showAbstractEditorMenuItems + showAnalyzedAbstractEditorMenuItems + showSpecParseTableMenuItems),
        MenuItemRepr.menu("Concrete", showConcreteEditorMenuItems + showAnalyzedConcreteEditorMenuItems)
      )
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Transform",
        MenuItemRepr.menu("Abstract", showAbstractResourceMenuItems + showAnalyzedAbstractResourceMenuItems + showSpecParseTableMenuItems),
        MenuItemRepr.menu("Concrete", showConcreteResourceMenuItems + showAnalyzedConcreteResourceMenuItems)
      )
    )
  )
}
