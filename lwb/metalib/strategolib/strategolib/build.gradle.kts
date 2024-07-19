import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

// FIXME: org.metaborg:strategolib in Spoofax 3 conflicts with a same named package in Spoofax 2 (from Stratego)
//  Use a devenv prefix?

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

    api(libs.metaborg.pie.task.archive)

    // Required to fix error: Cannot create Launcher without at least one TestEngine
    //  even though there are no tests in this project
    testImplementation(libs.junit)
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
val spoofax2DevenvVersion = "2.6.0-SNAPSHOT"  // TODO
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
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:strategolib:$spoofax2DevenvVersion"))
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
