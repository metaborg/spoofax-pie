import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

// FIXME: org.metaborg:gpp in Spoofax 3 conflicts with a same named package in Spoofax 2 (from Stratego)
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

    api(project(":strategolib"))
    api(libs.metaborg.pie.task.archive)

    // Required to fix error: Cannot create Launcher without at least one TestEngine
    //  even though there are no tests in this project
    testImplementation(libs.junit)
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
val spoofax2DevenvVersion = "2.6.0-SNAPSHOT"  // TODO
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
            languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:gpp:$spoofax2DevenvVersion"))
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
