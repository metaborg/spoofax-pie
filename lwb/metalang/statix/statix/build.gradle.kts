import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType
import mb.spoofax.common.*

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
    api(platform(libs.metaborg.platform))

    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)

    implementation(libs.spoofax3.spoofax2.common)

    testImplementation(libs.junit)
    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}

languageProject {
    shared {
        name("Statix")
        addFileExtensions("stx", "stxtest")
        defaultClassPrefix("Statix")
        defaultPackageId("mb.statix")
    }
    compilerInput {
        withParser().run {
            startSymbol("Start")
        }
        withStyler()
        withConstraintAnalyzer().run {
            enableNaBL2(true)
            enableStatix(false)
            multiFile(true)
        }
        withStrategoRuntime().run {
            addStrategyPackageIds("statix.lang.strategies")
            addStrategyPackageIds("statix.lang.trans")
            addInteropRegisterersByReflection("statix.lang.strategies.InteropRegisterer")
            addInteropRegisterersByReflection("statix.lang.trans.InteropRegisterer")
            addSpoofax2Primitives(true)
            addStatixPrimitives(true) // Requires the STX_compare_patterns primitive.
        }
    }
}
spoofax2BasedLanguageProject {
    compilerInput {
        withParser()
        withStyler()
        withConstraintAnalyzer()
        withStrategoRuntime().run {
            copyCtree(false)
            copyClasses(true)
        }
        project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:statix.lang:${ext["spoofax2DevenvVersion"]}"))
    }
}

val packageId = "mb.statix"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"
languageAdapterProject {
    compilerInput {
        withGetSourceFiles().run {
            extendGetSourceFilesTaskDef(spoofaxTaskPackageId, "StatixGetSourceFiles")
            baseGetSourceFilesTaskDef(spoofaxTaskPackageId, "BaseStatixGetSourceFiles")
        }
        withParser().run {
            // Wrap Parse task
            extendParseTaskDef(spoofaxTaskPackageId, "StatixParseWrapper")
        }
        withStyler()
        withConstraintAnalyzer().run {
            // Wrap AnalyzeMulti and rename base task
            baseAnalyzeMultiTaskDef(spoofaxTaskPackageId, "BaseStatixAnalyzeMulti")
            extendAnalyzeMultiTaskDef(spoofaxTaskPackageId, "StatixAnalyzeMultiWrapper")
        }
        withStrategoRuntime()
        withReferenceResolution().run {
            resolveStrategy("nabl2--editor-resolve")
        }
//    withHover().run {
//      hoverStrategy("nabl2--editor-hover")
//    }
        project.configureCompilerInput()
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val commandPackageId = "$packageId.command"

    // Symbols
    addLineCommentSymbols("//")
    addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
    addBracketSymbols(BracketSymbols('[', ']'))
    addBracketSymbols(BracketSymbols('{', '}'))
    addBracketSymbols(BracketSymbols('(', ')'))

    // Extend component
    baseComponent(packageId, "BaseStatixComponent")
    extendComponent(packageId, "StatixComponent")

    // Wrap CheckMulti and rename base tasks
    isMultiFile(true)
    baseCheckTaskDef(spoofaxTaskPackageId, "BaseStatixCheck")

    addTaskDefs(taskPackageId, "StatixPrettyPrint")

    addTaskDefs(taskPackageId, "StatixCompileModule")
    addTaskDefs(taskPackageId, "StatixCompileProject")
    addTaskDefs(taskPackageId, "StatixCompileAndMergeProject")
    addTaskDefs(taskPackageId, "StatixCompileSpec")

    // Evaluate test
    val evaluateTest = TypeInfo.of(taskPackageId, "StatixEvaluateTest")
    addTaskDefs(evaluateTest)
    val evaluateTestCommand = CommandDefRepr.builder()
        .type(commandPackageId, evaluateTest.id() + "Command")
        .taskDefType(evaluateTest)
        .argType(evaluateTest.appendToId(".Args"))
        .displayName("Evaluate test")
        .description("Evaluates a .stxtest file and shows the test result")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addAllParams(listOf(
            ParamRepr.of("rootDirectory", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
            ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ReadableResource))
        )).build()
    addCommandDefs(evaluateTestCommand)

    // Menu bindings
    val mainAndEditorMenu = listOf(
        MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(evaluateTestCommand).readableResourceRequired().enclosingProjectRequired().build()),
        MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(evaluateTestCommand).readableResourceRequired().enclosingProjectRequired().build())
    )
    addAllMainMenuItems(mainAndEditorMenu)
    addAllEditorContextMenuItems(mainAndEditorMenu)
    addResourceContextMenuItems(
        MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(evaluateTestCommand).fileRequired().enclosingProjectRequired().build()),
        MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(evaluateTestCommand).fileRequired().enclosingProjectRequired().build())
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
