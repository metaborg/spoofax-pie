import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api("org.metaborg:pie.task.archive")
}

languageProject {
    shared {
        name("StrategoLib")
        defaultClassPrefix("StrategoLib")
        defaultPackageId("mb.strategolib")
    }
    compilerInput {
        withStrategoRuntime().run {
            addStrategyPackageIds("strategolib.trans")
            addInteropRegisterersByReflection("strategolib.trans.InteropRegisterer")
        }
    }
}
spoofax2BasedLanguageProject {
    compilerInput {
        withStrategoRuntime().run {
            copyCtree(false)
            copyClasses(true)
        }
        project.run {
            addAdditionalCopyResources(
                "src-gen/java/strategolib/trans/strategolib.str2lib"
            )
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:strategolib:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

languageAdapterProject {
    compilerInput {
        project.configureCompilerInput()
        withExports().run {
            addFileExport("Stratego", "src-gen/java/strategolib/trans/strategolib.str2lib")
        }
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val packageId = "mb.strategolib"

    // Extend component
    baseComponent(packageId, "BaseStrategoLibComponent")
    extendComponent(packageId, "StrategoLibComponent")
}
