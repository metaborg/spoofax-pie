import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.EnclosingCommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.common.*

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.spoofax3.spt.api)

    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)
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
            addStrategyPackageIds("org.metaborg.meta.lang.spt.strategies")
            addInteropRegisterersByReflection("org.metaborg.meta.lang.spt.trans.InteropRegisterer")
            addInteropRegisterersByReflection("org.metaborg.meta.lang.spt.strategies.InteropRegisterer")
        }
    }
}
val spoofax2DevenvVersion = "2.6.0-SNAPSHOT"  // TODO
spoofax2BasedLanguageProject {
    compilerInput {
        withParser()
        withStyler()
        withStrategoRuntime().run {
            copyClasses(true)
        }
        project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.spt:$spoofax2DevenvVersion"))
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
    compositionGroup("mb.spoofax.lwb")

    // Symbols
    addLineCommentSymbols("//")
    addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
    addBracketSymbols(BracketSymbols('[', ']'))
    addBracketSymbols(BracketSymbols('{', '}'))
    addBracketSymbols(BracketSymbols('(', ')'))

    // Extend resources component and add modules
    baseResourcesComponent(packageId, "BaseSptResourcesComponent")
    extendResourcesComponent(packageId, "SptResourcesComponent")
    addAdditionalResourcesModules("$packageId.resource", "SptTestCaseResourceModule")

    // Extend component and add modules
    baseComponent(packageId, "BaseSptComponent")
    extendComponent(packageId, "SptComponent")
    addAdditionalModules("$packageId.fromterm", "ExpectationFromTermsModule")

    // Extend participant
    baseParticipant(packageId, "BaseSptParticipant")
    extendParticipant(packageId, "SptParticipant")

    // Wrap Check and rename base tasks
    isMultiFile(false)
    baseCheckTaskDef(taskPackageId, "BaseSptCheck")
    baseCheckMultiTaskDef(taskPackageId, "BaseSptCheckMulti")
    extendCheckTaskDef(taskPackageId, "SptCheck")

    // Internal task definitions
    val check = TypeInfo.of(taskPackageId, "SptCheck")
    addTaskDefs(check)
    val runTestSuite = TypeInfo.of(taskPackageId, "SptRunTestSuite")
    val runTestSuites = TypeInfo.of(taskPackageId, "SptRunTestSuites")
    addTaskDefs(runTestSuite, runTestSuites)

    // Add test running tasks
    val showTestSuiteResults = TypeInfo.of(taskPackageId, "SptShowTestSuiteResults")
    val showTestResults = TypeInfo.of(taskPackageId, "SptShowTestSuitesResults")
    addTaskDefs(showTestSuiteResults, showTestResults)

    // Add test running commands
    val showTestSuiteCommand = CommandDefRepr.builder()
        .type(commandPackageId, showTestSuiteResults.id() + "Command")
        .taskDefType(showTestSuiteResults)
        .argType(TypeInfo.of(taskPackageId, "SptShowTestSuiteResults.Args"))
        .displayName("Run SPT tests")
        .description("Run the SPT tests in this file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "rootDir",
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
    val showTestSuitesCommand = CommandDefRepr.builder()
        .type(commandPackageId, showTestResults.id() + "Command")
        .taskDefType(showTestResults)
        .argType(TypeInfo.of(taskPackageId, "SptShowTestSuitesResults.Args"))
        .displayName("Run SPT tests")
        .description("Run the SPT tests in this directory")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
        .addAllParams(
            listOf(
                ParamRepr.of(
                    "rootDir",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)
                ),
                ParamRepr.of(
                    "directory",
                    TypeInfo.of("mb.resource.hierarchical", "ResourcePath"),
                    true,
                    ArgProviderRepr.context(CommandContextType.Directory)
                )
            )
        )
        .build()
    addCommandDefs(showTestSuiteCommand, showTestSuitesCommand)

    // Menu bindings
    val mainAndEditorMenu = listOf(
        CommandActionRepr.builder().manualOnce(showTestSuiteCommand).fileRequired().enclosingProjectRequired().buildItem()
    )
    addAllMainMenuItems(mainAndEditorMenu)
    addAllEditorContextMenuItems(mainAndEditorMenu)
    addResourceContextMenuItems(
        CommandActionRepr.builder().manualOnce(showTestSuiteCommand).fileRequired().enclosingProjectRequired().buildItem(),
        CommandActionRepr.builder().manualOnce(showTestSuitesCommand).directoryRequired().enclosingProjectRequired().buildItem()
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
