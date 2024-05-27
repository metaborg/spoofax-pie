import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageProject {
    shared {
        name("LibStatix")
        defaultClassPrefix("LibStatix")
        defaultPackageId("mb.libstatix")
    }
    compilerInput {
    }
}
spoofax2BasedLanguageProject {
    compilerInput {
        project.run {
            addAdditionalCopyResources("trans/**/*.str", "trans/**/*.str2", "src-gen/**/*.str", "src-gen/**/*.str2")
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:statix.runtime:${ext["spoofax2DevenvVersion"]}"))
        }
    }
}

languageAdapterProject {
    compilerInput {
        project.configureCompilerInput()
        withExports().run {
            addDirectoryExport("Stratego", "trans")
            addDirectoryExport("Stratego", "src-gen")
        }
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")
}
