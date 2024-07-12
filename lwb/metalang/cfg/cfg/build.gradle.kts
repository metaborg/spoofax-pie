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

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(libs.metaborg.platform))

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

languageProject {
    shared {
        name("CFG")
        defaultClassPrefix("Cfg")
        defaultPackageId("mb.cfg")
    }
    compilerInput {
        withParser().run {
            startSymbol("Start")
        }
        withStyler()
        withConstraintAnalyzer().run {
            enableNaBL2(false)
            enableStatix(true)
            multiFile(false)
        }
        withStrategoRuntime()
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
            copyClasses(false)
        }
        project.languageSpecificationDependency(GradleDependency.project(":cfg.spoofax2"))
    }
}

languageAdapterProject {
    compilerInput {
        withParser()
        withStyler()
        withConstraintAnalyzer()
        withStrategoRuntime()
        withReferenceResolution().run {
            resolveStrategy("editor-resolve")
        }
//    withHover().run {
//      hoverStrategy("editor-hover")
//    }
        project.configureCompilerInput()
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val packageId = "mb.cfg"
    val taskPackageId = "$packageId.task"

    // Symbols
    addLineCommentSymbols("//")
    addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
    addBracketSymbols(BracketSymbols('[', ']'))
    addBracketSymbols(BracketSymbols('{', '}'))
    addBracketSymbols(BracketSymbols('(', ')'))

    addAdditionalModules(packageId, "CfgCustomizerModule");

    // Config object creation tasks.
    val normalize = TypeInfo.of(taskPackageId, "CfgNormalize")
    val toObject = TypeInfo.of(taskPackageId, "CfgToObject")
    val rootDirectoryToObject = TypeInfo.of(taskPackageId, "CfgRootDirectoryToObject")
    addTaskDefs(normalize, toObject, rootDirectoryToObject)

    // Manual multi-file check implementation.
    isMultiFile(false)
    val spoofaxTaskPackageId = "$taskPackageId.spoofax"
    baseCheckTaskDef(spoofaxTaskPackageId, "BaseCfgCheck")
    extendCheckTaskDef(spoofaxTaskPackageId, "CfgCheck")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
