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

    api(libs.spoofax3.spoofax.common)
    api(libs.spoofax3.compiler)

    compileOnly(libs.derive4j.annotation)

    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)
    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
}

val packageId = "mb.dynamix"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"
val debugTaskPackageId = "$taskPackageId.debug"
val commandPackageId = "$packageId.command"

languageProject {
    shared {
        name("Dynamix")
        defaultClassPrefix("Dynamix")
        defaultPackageId("mb.dynamix")
        addFileExtensions("dx")
    }
    compilerInput {
        withParser().run {
            startSymbol("Start")
        }
        withStyler()
        withConstraintAnalyzer().run {
            enableNaBL2(false)
            enableStatix(true)
            multiFile(true)
        }
        withStrategoRuntime().run {
            addStrategyPackageIds("dynamix.strategies")
            addInteropRegisterersByReflection("dynamix.strategies.InteropRegisterer")
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
        project.languageSpecificationDependency(GradleDependency.project(":dynamix.spoofax2"))
    }
}

languageAdapterProject {
    compilerInput {
        withParser().run {
            // Wrap Parse task
            extendParseTaskDef(spoofaxTaskPackageId, "DynamixParseWrapper")
        }
        withStyler()
        withConstraintAnalyzer().run {
            // Wrap AnalyzeMulti and rename base task
            baseAnalyzeMultiTaskDef(spoofaxTaskPackageId, "BaseDynamixAnalyzeMulti")
            extendAnalyzeMultiTaskDef(spoofaxTaskPackageId, "DynamixAnalyzeMultiWrapper")
        }
        withStrategoRuntime()
        withReferenceResolution().run {
            resolveStrategy("editor-resolve")
        }
        withHover().run {
            hoverStrategy("editor-hover")
        }
        withGetSourceFiles().run {
            extendGetSourceFilesTaskDef(spoofaxTaskPackageId, "DynamixGetSourceFiles")
            baseGetSourceFilesTaskDef(spoofaxTaskPackageId, "BaseDynamixGetSourceFiles")
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

    baseComponent(packageId, "BaseDynamixComponent")
    extendComponent(packageId, "DynamixComponent")

    addTaskDefs(taskPackageId, "DynamixCompileModule")
    addTaskDefs(taskPackageId, "DynamixCompileProject")
    addTaskDefs(taskPackageId, "DynamixCompileAndMergeProject")
    addTaskDefs(taskPackageId, "DynamixPrettyPrint")

    isMultiFile(true)

    val showMerged = TypeInfo.of(debugTaskPackageId, "DynamixShowMerged")
    addTaskDefs(
        showMerged
    )

    val showMergedCommand = CommandDefRepr.builder()
        .type(commandPackageId, showMerged.id() + "Command")
        .taskDefType(showMerged)
        .argType(showMerged.appendToId(".Args"))
        .displayName("Show normalized merged form")
        .description("Shows the normalized and merged form in which definitions and calls are fully qualified")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "rootDirectory",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of(
                    "file",
                    TypeInfo.of("mb.resource", "ResourceKey"),
                    true,
                    ArgProviderRepr.context(CommandContextType.File)
                )
            )
        )
        .build()
    val showMergedCommandMenuItem = CommandActionRepr.builder()
        .manualOnce(showMergedCommand)
        .fileRequired()
        .enclosingProjectRequired()
        .buildItem()

    val commands = listOf(
        showMergedCommand
    )
    addAllCommandDefs(commands)

    val menuItems = listOf(
        MenuItemRepr.menu("Evaluate", listOf(showMergedCommandMenuItem))
    )
    addAllMainMenuItems(menuItems)
    addAllEditorContextMenuItems(menuItems)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
