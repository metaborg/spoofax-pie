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
        name("LibStatix")
        defaultClassPrefix("LibStatix")
        defaultPackageId("mb.libstatix")
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
            addAdditionalCopyResources("trans/**/*.str", "trans/**/*.str2", "src-gen/**/*.str", "src-gen/**/*.str2")
            languageSpecificationDependency(libs.statix.runtime.get().toGradleDependency())
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
