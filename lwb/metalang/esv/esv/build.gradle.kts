import mb.spoofax.common.BlockCommentSymbols
import mb.spoofax.common.BracketSymbols
import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.TypeInfo

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)
}

languageProject {
    shared {
        name("ESV")
        defaultClassPrefix("Esv")
        defaultPackageId("mb.esv")
    }
    compilerInput {
        withParser().run {
            startSymbol("Module")
        }
        withStyler()
        withStrategoRuntime().run {
            addStrategyPackageIds("org.metaborg.meta.lang.esv.trans")
            addInteropRegisterersByReflection("org.metaborg.meta.lang.esv.trans.InteropRegisterer")
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
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.esv:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

val packageId = "mb.esv"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"

languageAdapterProject {
    compilerInput {
        withParser().run {
            // Wrap Parse task
            extendParseTaskDef(spoofaxTaskPackageId, "EsvParseWrapper")
        }
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
    addBracketSymbols(BracketSymbols('(', ')'))
    addBracketSymbols(BracketSymbols('<', '>'))

    // Extend component
    baseComponent(packageId, "BaseEsvComponent")
    extendComponent(packageId, "EsvComponent")

    // Wrap CheckMulti and rename base tasks
    isMultiFile(true)
    baseCheckTaskDef(spoofaxTaskPackageId, "BaseEsvCheck")
    baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseEsvCheckMulti")
    extendCheckMultiTaskDef(spoofaxTaskPackageId, "EsvCheckMultiWrapper")

    // Internal task definitions
    val check = TypeInfo.of(taskPackageId, "EsvCheck")
    val compile = TypeInfo.of(taskPackageId, "EsvCompile")
    addTaskDefs(check, compile)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
