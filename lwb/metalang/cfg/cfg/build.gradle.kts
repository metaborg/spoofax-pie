import mb.spoofax.common.BlockCommentSymbols
import mb.spoofax.common.BracketSymbols
import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.TypeInfo

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(compositeBuild("spoofax.common"))
    api(compositeBuild("spoofax.compiler"))

    compileOnly("org.derive4j:derive4j-annotation")

    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly("com.google.code.findbugs:jsr305")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.immutables:value-annotations")
    annotationProcessor("org.immutables:value")
    annotationProcessor("org.derive4j:derive4j")
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
