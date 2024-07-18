import mb.spoofax.common.BlockCommentSymbols
import mb.spoofax.common.BracketSymbols
import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.adapter.data.ArgProviderRepr
import mb.spoofax.compiler.adapter.data.CommandActionRepr
import mb.spoofax.compiler.adapter.data.CommandDefRepr
import mb.spoofax.compiler.adapter.data.MenuItemRepr
import mb.spoofax.compiler.adapter.data.ParamRepr
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.sdf2table)
    api(libs.sdf2parenthesize)

    testImplementation(libs.junit)
    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}

languageProject {
    shared {
        name("SDF3")
        defaultClassPrefix("Sdf3")
        defaultPackageId("mb.sdf3")
        addFileExtensions("sdf3", "tmpl")
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
            addStrategyPackageIds("org.metaborg.meta.lang.template.strategies")
            addStrategyPackageIds("org.metaborg.meta.lang.template.trans")
            addInteropRegisterersByReflection("org.metaborg.meta.lang.template.strategies.InteropRegisterer")
            addInteropRegisterersByReflection("org.metaborg.meta.lang.template.trans.InteropRegisterer")
            baseStrategoRuntimeBuilderFactory("mb.sdf3.stratego", "BaseSdf3StrategoRuntimeBuilderFactory")
            extendStrategoRuntimeBuilderFactory("mb.sdf3.stratego", "Sdf3StrategoRuntimeBuilderFactory")
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
            copyClasses(true)
        }
        project.run {
            addAdditionalCopyResources("target/metaborg/EditorService-pretty.pp.af")
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.template:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

val packageId = "mb.sdf3"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"

languageAdapterProject {
    compilerInput {
        withParser().run {
            // Wrap Parse task
            extendParseTaskDef(spoofaxTaskPackageId, "Sdf3ParseWrapper")
        }
        withStyler()
        withStrategoRuntime()
        withConstraintAnalyzer()
        withReferenceResolution().run {
            resolveStrategy("statix-editor-resolve")
        }
//    withHover().run {
//      hoverStrategy("statix-editor-hover")
//    }
        withGetSourceFiles().run {
            baseGetSourceFilesTaskDef(spoofaxTaskPackageId, "BaseSdf3GetSourceFiles")
            extendGetSourceFilesTaskDef(spoofaxTaskPackageId, "Sdf3GetSourceFilesWrapper")
        }
        project.configureCompilerInput()
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    // Symbols
    addLineCommentSymbols("//")
    addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
    addBracketSymbols(BracketSymbols('[', ']'))
    addBracketSymbols(BracketSymbols('{', '}'))
    addBracketSymbols(BracketSymbols('(', ')'))

    // Extend component
    baseComponent(packageId, "BaseSdf3Component")
    extendComponent(packageId, "Sdf3Component")


    /// Tasks
    // Utility task definitions
    val desugar = TypeInfo.of(taskPackageId, "Sdf3Desugar")
    val prettyPrint = TypeInfo.of(taskPackageId, "Sdf3PrettyPrint")
    addTaskDefs(desugar, prettyPrint)

    // Per-module transformations
    val toCompletionColorer = TypeInfo.of(taskPackageId, "Sdf3ToCompletionColorer")
    val toCompletionRuntime = TypeInfo.of(taskPackageId, "Sdf3ToCompletionRuntime")
    val toCompletion = TypeInfo.of(taskPackageId, "Sdf3ToCompletion")
    val toSignature = TypeInfo.of(taskPackageId, "Sdf3ToSignature")
    val toDynsemSignature = TypeInfo.of(taskPackageId, "Sdf3ToDynsemSignature")
    val toPrettyPrinter = TypeInfo.of(taskPackageId, "Sdf3ToPrettyPrinter")
    val toPermissive = TypeInfo.of(taskPackageId, "Sdf3ToPermissive")
    val toNormalForm = TypeInfo.of(taskPackageId, "Sdf3ToNormalForm")
    val preStatix = TypeInfo.of(taskPackageId, "Sdf3PreStatix")
    val postStatix = TypeInfo.of(taskPackageId, "Sdf3PostStatix")
    val indexAst = TypeInfo.of(taskPackageId, "Sdf3IndexAst")
    addTaskDefs(
        toCompletionColorer, toCompletionRuntime, toCompletion, toSignature, toDynsemSignature,
        toPrettyPrinter, toPermissive, toNormalForm, preStatix, postStatix, indexAst
    )

    // Per-spec tasks
    val specTaskPackageId = "$taskPackageId.spec"
    val checkSpec = TypeInfo.of(specTaskPackageId, "Sdf3CheckSpec")
    val specToParseTable = TypeInfo.of(specTaskPackageId, "Sdf3SpecToParseTable")
    val parseTableToParenthesizer = TypeInfo.of(specTaskPackageId, "Sdf3ParseTableToParenthesizer")
    val parseTableToFile = TypeInfo.of(specTaskPackageId, "Sdf3ParseTableToFile")
    addTaskDefs(checkSpec, specToParseTable, parseTableToParenthesizer, parseTableToFile)

    // Debugging task definitions
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

    // Additional tasks
    addTaskDefs(TypeInfo.of(taskPackageId, "Sdf3ParseTableFromFile"))

    // Extend CheckMulti and GetSourceFiles, rename base tasks.
    isMultiFile(true)
    baseCheckTaskDef(spoofaxTaskPackageId, "BaseSdf3Check")
    baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseSdf3CheckMulti")
    extendCheckMultiTaskDef(spoofaxTaskPackageId, "Sdf3CheckMultiWrapper")

    /// Commands
    val commandPackageId = "$packageId.command"

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
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "file",
                    TypeInfo.of("mb.resource", "ResourceKey"),
                    true,
                    ArgProviderRepr.context(CommandContextType.File)
                ),
                ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
            )
        )
        .build()

    fun showAnalyzedCommand(taskDefType: TypeInfo, resultName: String) = CommandDefRepr.builder()
        .type(commandPackageId, taskDefType.id() + "Command")
        .taskDefType(taskDefType)
        .argType(showAnalyzedAbstractTaskDef.appendToId(".Args"))
        .displayName("Show $resultName")
        .description("Shows the $resultName of the file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "project",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of(
                    "file",
                    TypeInfo.of("mb.resource", "ResourceKey"),
                    true,
                    ArgProviderRepr.context(CommandContextType.File)
                ),
                ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
            )
        )
        .build()

    val showDesugarCommand = showCommand(showDesugar, "desugared")
    val showPermissiveCommand = showCommand(showPermissive, "permissive grammar")
    val showNormalFormCommand = CommandDefRepr.builder()
        .type(commandPackageId, showNormalForm.id() + "Command")
        .taskDefType(showNormalForm)
        .argType(showNormalForm.appendToId(".Args"))
        .displayName("Show normal-form")
        .description("Shows the normal-form of the file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "root",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of(
                    "file",
                    TypeInfo.of("mb.resource", "ResourceKey"),
                    true,
                    ArgProviderRepr.context(CommandContextType.File)
                ),
                ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
            )
        )
        .build()
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
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "root",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
            )
        )
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
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "root",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                )
            )
        )
        .build()
    addCommandDefs(
        showSpecParseTableCommand,
        showSpecParenthesizerCommand
    )
    val showSpecParseTableOnceMenuItem = CommandActionRepr.builder().manualOnce(showSpecParseTableCommand).fileRequired().enclosingProjectRequired().buildItem()
    val showSpecParseTableContinuousMenuItem = CommandActionRepr.builder().manualContinuous(showSpecParseTableCommand).fileRequired().enclosingProjectRequired().buildItem()
    val showSpecParseTableMenuItems = listOf(showSpecParseTableOnceMenuItem, showSpecParseTableContinuousMenuItem)


    // Show (debugging) menu command actions
    fun showManualOnce(commandDef: CommandDefRepr, concrete: Boolean) =
        CommandActionRepr.builder().manualOnce(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().buildItem()

    fun showManualContinuous(commandDef: CommandDefRepr, concrete: Boolean) =
        CommandActionRepr.builder().manualContinuous(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().buildItem()

    fun showAnalyzedManualOnce(commandDef: CommandDefRepr, concrete: Boolean) =
        CommandActionRepr.builder().manualOnce(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().enclosingProjectRequired().buildItem()

    fun showAnalyzedManualContinuous(commandDef: CommandDefRepr, concrete: Boolean) =
        CommandActionRepr.builder().manualContinuous(commandDef, mapOf(Pair("concrete", concrete.toString()))).fileRequired().enclosingProjectRequired().buildItem()

    val showAbstractEditorMenuItems =
        showCommands.flatMap { listOf(showManualOnce(it, false), showManualContinuous(it, false)) }
    val showConcreteEditorMenuItems =
        showCommands.flatMap { listOf(showManualOnce(it, true), showManualContinuous(it, true)) }
    val showAbstractResourceMenuItems = showCommands.map { showManualOnce(it, false) }
    val showConcreteResourceMenuItems = showCommands.map { showManualOnce(it, true) }
    val showAnalyzedAbstractEditorMenuItems = showAnalyzedCommands.flatMap {
        listOf(
            showAnalyzedManualOnce(it, false),
            showAnalyzedManualContinuous(it, false)
        )
    }
    val showAnalyzedConcreteEditorMenuItems = showAnalyzedCommands.flatMap {
        listOf(
            showAnalyzedManualOnce(it, true),
            showAnalyzedManualContinuous(it, true)
        )
    }
    val showAnalyzedAbstractResourceMenuItems = showAnalyzedCommands.map { showAnalyzedManualOnce(it, false) }
    val showAnalyzedConcreteResourceMenuItems = showAnalyzedCommands.map { showAnalyzedManualOnce(it, true) }


    // Menu bindings
    val mainAndEditorMenu = listOf(
        MenuItemRepr.menu("Debug",
            MenuItemRepr.menu("Transform",
                MenuItemRepr.menu("Abstract",
                    showAbstractEditorMenuItems + showAnalyzedAbstractEditorMenuItems + showSpecParseTableMenuItems
                ),
                MenuItemRepr.menu("Concrete", showConcreteEditorMenuItems + showAnalyzedConcreteEditorMenuItems)
            )
        )
    )
    addAllMainMenuItems(mainAndEditorMenu)
    addAllEditorContextMenuItems(mainAndEditorMenu)
    addResourceContextMenuItems(
        MenuItemRepr.menu("Debug",
            MenuItemRepr.menu("Transform",
                MenuItemRepr.menu("Abstract",
                    showAbstractResourceMenuItems + showAnalyzedAbstractResourceMenuItems + showSpecParseTableMenuItems
                ),
                MenuItemRepr.menu("Concrete", showConcreteResourceMenuItems + showAnalyzedConcreteResourceMenuItems)
            )
        )
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
