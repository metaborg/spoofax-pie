import mb.spoofax.compiler.adapter.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependencies
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.core.CoordinateRequirement
import mb.spoofax.core.Version

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
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
        project.run {
            addAdditionalCopyResources(
                "trans/**/*.str",
                "trans/**/*.str2",
                "editor/**/*.esv"
            )
            languageSpecificationDependency(libs.meta.lib.spoofax.get().toGradleDependency())
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
