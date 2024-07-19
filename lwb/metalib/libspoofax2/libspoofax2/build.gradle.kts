import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
    // Required to fix error: Cannot create Launcher without at least one TestEngine
    //  even though there are no tests in this project
    testImplementation(libs.junit)
}

languageProject {
    shared {
        name("LibSpoofax2")
        defaultClassPrefix("LibSpoofax2")
        defaultPackageId("mb.libspoofax2")
    }
    compilerInput {
    }
}
val spoofax2DevenvVersion = "2.6.0-SNAPSHOT"  // TODO
spoofax2BasedLanguageProject {
    compilerInput {
        project.run {
            addAdditionalCopyResources(
                "trans/**/*.str",
                "trans/**/*.str2",
                "editor/**/*.esv"
            )
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:meta.lib.spoofax:$spoofax2DevenvVersion"))
        }
    }
}

languageAdapterProject {
    compilerInput {
        project.configureCompilerInput()
        withExports().run {
            addDirectoryExport("Stratego", "trans")
            addDirectoryExport("ESV", "editor")
        }
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
