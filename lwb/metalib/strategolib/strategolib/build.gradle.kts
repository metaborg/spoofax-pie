import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependencies
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.core.CoordinateRequirement
import mb.spoofax.core.Version

// FIXME: org.metaborg:strategolib in Spoofax 3 conflicts with a same named package in Spoofax 2 (from Stratego)
//  Use a devenv prefix?

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.pie.task.archive)
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

fun ModuleDependency.toGradleDependency(): GradleDependency {
    return GradleDependencies.module(
        CoordinateRequirement(
            this@toGradleDependency.group,
            this@toGradleDependency.name,
            Version.parse(this@toGradleDependency.version),
        )
    )
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
            languageSpecificationDependency(libs.strategolib.get().toGradleDependency())
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
