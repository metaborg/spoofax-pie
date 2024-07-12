import mb.spoofax.common.BlockCommentSymbols
import mb.spoofax.common.BracketSymbols
import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.adapter.data.ArgProviderRepr
import mb.spoofax.compiler.adapter.data.CommandActionRepr
import mb.spoofax.compiler.adapter.data.CommandDefRepr
import mb.spoofax.compiler.adapter.data.MenuItemRepr
import mb.spoofax.compiler.adapter.data.ParamRepr
import mb.spoofax.compiler.language.ParserVariant
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(libs.metaborg.platform))
    testImplementation(platform(libs.metaborg.platform))

    api(libs.metaborg.util)
    api(libs.stratego.build)
    api(libs.metaborg.pie.task.archive)

    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)

    implementation(libs.commons.io)

    testImplementation(libs.spoofax3.test)
    testImplementation(libs.metaborg.pie.task.java)
    testImplementation(libs.metaborg.pie.task.archive)
    testImplementation(libs.spoofax3.test)
    testImplementation(project(":strategolib"))
    testAnnotationProcessor(libs.dagger.compiler)
    testCompileOnly(libs.checkerframework.android)
}

languageProject {
    shared {
        name("Stratego")
        defaultClassPrefix("Stratego")
        defaultPackageId("mb.str")
        addFileExtensions("str", "str2")
    }
    compilerInput {
        withParser().run {
            startSymbol("Module")
            variant(ParserVariant.jsglr2(ParserVariant.Jsglr2Preset.Recovery))
        }
        withStyler()
        withStrategoRuntime().run {
            addStrategyPackageIds("stratego.lang.trans")
            addInteropRegisterersByReflection("stratego.lang.trans.InteropRegisterer")
            addStrategyPackageIds("stratego.lang.strategies")
            addInteropRegisterersByReflection("stratego.lang.strategies.InteropRegisterer")
        }
    }
}
spoofax2BasedLanguageProject {
    compilerInput {
        withParser()
        withStyler()
        withStrategoRuntime().run {
            copyCtree(false)
            copyClasses(true)
        }
        project.run {
            addAdditionalCopyResources(
                "syntax/**/*.sdf3",
                "src-gen/syntax/**/*.sdf3"
            )
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:stratego.lang:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

val packageId = "mb.str"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"

languageAdapterProject {
    compilerInput {
        withParser().run {
            // Wrap Parse task
            extendParseTaskDef(spoofaxTaskPackageId, "StrategoParseWrapper")
        }
        withStyler()
        withStrategoRuntime()
        withExports().run {
            addDirectoryExport("SDF3", "syntax")
            addDirectoryExport("SDF3", "src-gen/syntax")
        }
        project.configureCompilerInput()
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val incrPackageId = "$packageId.incr"
    val commandPackageId = "$packageId.command"

    // Symbols
    addLineCommentSymbols("//")
    addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
    addBracketSymbols(BracketSymbols('[', ']'))
    addBracketSymbols(BracketSymbols('{', '}'))
    addBracketSymbols(BracketSymbols('(', ')'))

    // Extend component
    baseComponent(packageId, "BaseStrategoComponent")
    extendComponent(packageId, "StrategoComponent")
    addAdditionalModules(incrPackageId, "StrategoIncrModule")

    // Wrap CheckMulti and rename base tasks
    isMultiFile(true)
    baseCheckTaskDef(spoofaxTaskPackageId, "BaseStrategoCheck")
    baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseStrategoCheckMulti")
    extendCheckMultiTaskDef(spoofaxTaskPackageId, "StrategoCheckMultiWrapper")

    // Stratego incremental compiler task definitions
    val strBuildTaskPackageId = "mb.stratego.build.strincr.task"
    addTaskDefs(strBuildTaskPackageId, "Back")
    addTaskDefs(strBuildTaskPackageId, "Check")
    addTaskDefs(strBuildTaskPackageId, "CheckModule")
    addTaskDefs(strBuildTaskPackageId, "CheckOpenModule")
    addTaskDefs(strBuildTaskPackageId, "Compile")
    addTaskDefs(strBuildTaskPackageId, "Front")
    addTaskDefs(strBuildTaskPackageId, "FrontSplit")
    addTaskDefs(strBuildTaskPackageId, "Resolve")
    addTaskDefs(strBuildTaskPackageId, "CopyLibraryClassFiles")
    addTaskDefs(strBuildTaskPackageId, "CompileDynamicRules")

    // Task definitions
    addTaskDefs(taskPackageId, "StrategoCompileToJava")
    addTaskDefs(taskPackageId, "StrategoCheck")
    val compileToJavaEditor = TypeInfo.of(taskPackageId, "StrategoEditorCompileToJava")
    addTaskDefs(compileToJavaEditor, compileToJavaEditor)
    addTaskDefs(taskPackageId, "StrategoPrettyPrint")

    // Compilation command
    val resourcePathType = TypeInfo.of("mb.resource.hierarchical", "ResourcePath")
    val compileToJavaCommand = CommandDefRepr.builder()
        .type(commandPackageId, compileToJavaEditor.id() + "Command")
        .taskDefType(compileToJavaEditor)
        .argType("mb.str.config", "StrategoCompileConfig")
        .displayName("Compile to Java")
        .description("Compiles Stratego source files to Java source files")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "rootDirectory",
                    resourcePathType,
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of(
                    "mainFile",
                    resourcePathType,
                    true,
                    ArgProviderRepr.context(CommandContextType.File)
                ),
                ParamRepr.of(
                    "includeDirs",
                    TypeInfo.of("mb.common.util", "ListView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.ListView.of()")
                ),
                ParamRepr.of(
                    "builtinLibs",
                    TypeInfo.of("mb.common.util", "ListView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.ListView.of()")
                ),
                ParamRepr.of(
                    "str2libraries",
                    TypeInfo.of("mb.common.util", "ListView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.ListView.of()")
                ),
                ParamRepr.of(
                    "extraCompilerArguments",
                    TypeInfo.of("org.metaborg.util.cmd", "Arguments"),
                    false,
                    ArgProviderRepr.value("new org.metaborg.util.cmd.Arguments()")
                ),
                ParamRepr.of(
                    "concreteSyntaxExtensionParseTables",
                    TypeInfo.of("mb.common.util", "MapView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.MapView.of()")
                ),
                ParamRepr.of(
                    "concreteSyntaxExtensionTransientParseTables",
                    TypeInfo.of("mb.common.util", "MapView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.MapView.of()")
                ),
                ParamRepr.of(
                    "sourceFileOrigins",
                    TypeInfo.of("mb.common.util", "ListView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.ListView.of()")
                ),
                ParamRepr.of("cacheDir", resourcePathType, false),
                ParamRepr.of("javaSourceFileOutputDir", resourcePathType, true),
                ParamRepr.of("javaClassFileOutputDir", resourcePathType, true),
                ParamRepr.of("outputJavaPackageId", TypeInfo.ofString(), true),
                ParamRepr.of("outputLibraryName", TypeInfo.ofString(), true),
                ParamRepr.of(
                    "javaClassPaths",
                    TypeInfo.of("mb.common.util", "ListView"),
                    false,
                    ArgProviderRepr.value("mb.common.util.ListView.of()")
                )
            )
        )
        .build()
    addCommandDefs(compileToJavaCommand)

    // Show (debugging) task definitions
    val debugTaskPackageId = "$taskPackageId.debug"
    val showParsedAst = TypeInfo.of(debugTaskPackageId, "StrategoShowParsedAst")
    val showDesugaredAst = TypeInfo.of(debugTaskPackageId, "StrategoShowDesugaredAst")
    addTaskDefs(showParsedAst, showDesugaredAst)
    // Show (debugging) commands
    fun showCommand(taskDefType: TypeInfo, resultName: String) = CommandDefRepr.builder()
        .type(commandPackageId, taskDefType.id() + "Command")
        .taskDefType(taskDefType)
        .argType(TypeInfo.of(debugTaskPackageId, "StrategoShowArgs"))
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
                ParamRepr.of(
                    "rootDirectoryHint",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false)
            )
        )
        .build()

    val showParsedAstCommand = showCommand(showParsedAst, "parsed AST")
    val showDesugaredAstCommand = showCommand(showDesugaredAst, "desugared AST")
    val showCommands = listOf(
        showParsedAstCommand,
        showDesugaredAstCommand
    )
    addAllCommandDefs(showCommands)

    // Menu bindings
    val mainAndEditorMenu = listOf(
        MenuItemRepr.menu(
            "Compile",
            CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
        ),
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
        MenuItemRepr.menu(
            "Compile",
            CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
        ),
        MenuItemRepr.menu("Debug",
            showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem()) }
        )
    )
}

// Additional dependencies which are injected into tests.
val classPathInjection = configurations.create("classPathInjection")
dependencies {
    classPathInjection(platform("$group:spoofax.depconstraints:$version"))
//    classPathInjection(platform(libs.metaborg.platform))
    classPathInjection(libs.strategoxt.strj)
}

tasks.test {
    // Pass classPathInjection to tests in the form of system properties
    dependsOn(classPathInjection)
    doFirst {
        // Wrap in doFirst to properly defer dependency resolution to the task execution phase.
        systemProperty(
            "classPath",
            classPathInjection.resolvedConfiguration.resolvedArtifacts.map { it.file }.joinToString(File.pathSeparator)
        )
    }
}
