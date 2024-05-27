import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(project(":strategolib"))
    api("org.metaborg:pie.task.archive")
}

languageProject {
    shared {
        name("Gpp")
        defaultClassPrefix("Gpp")
        defaultPackageId("mb.gpp")
    }
    compilerInput {
        withStrategoRuntime().run {
            addStrategyPackageIds("gpp.trans")
            addInteropRegisterersByReflection("gpp.trans.InteropRegisterer")
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
                "src-gen/java/gpp/trans/gpp.str2lib"
            )
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:gpp:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

languageAdapterProject {
    compilerInput {
        project.configureCompilerInput()
        withExports().run {
            addFileExport("Stratego", "src-gen/java/gpp/trans/gpp.str2lib")
        }
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val packageId = "mb.gpp"

    // Extend component
    baseComponent(packageId, "BaseGppComponent")
    extendComponent(packageId, "GppComponent")
}
