import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*
import mb.spoofax.compiler.util.GradleDependencies
import mb.spoofax.core.CoordinateRequirement
import mb.spoofax.core.Version

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {

}

languageProject {
    shared {
        name("Sdf3ExtStatix")
        defaultClassPrefix("Sdf3ExtStatix")
        defaultPackageId("mb.sdf3_ext_statix")
        fileExtensions(listOf()) // No file extensions.
    }
    compilerInput {
        withStrategoRuntime().run {
            addStrategyPackageIds("sdf3.ext.statix.strategies")
            addStrategyPackageIds("sdf3.ext.statix.trans")
            addInteropRegisterersByReflection("sdf3.ext.statix.trans.InteropRegisterer")
            addInteropRegisterersByReflection("sdf3.ext.statix.strategies.InteropRegisterer")
            baseStrategoRuntimeBuilderFactory("mb.sdf3_ext_statix.stratego", "BaseSdf3ExtStatixStrategoRuntimeBuilderFactory")
            extendStrategoRuntimeBuilderFactory("mb.sdf3_ext_statix.stratego", "Sdf3ExtStatixStrategoRuntimeBuilderFactory")
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
            copyClasses(true)
        }
        project.languageSpecificationDependency(libs.sdf3.extstatix.get().toGradleDependency())
    }
}

languageAdapterProject {
    compilerInput {
        withStrategoRuntime()
        project.configureCompilerInput()
    }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
    compositionGroup("mb.spoofax.lwb")

    val packageId = "mb.sdf3_ext_statix"
    val taskPackageId = "$packageId.task"

    addTaskDefs(
        TypeInfo.of(taskPackageId, "Sdf3ExtStatixGenerateStatix"),
        TypeInfo.of(taskPackageId, "Sdf3ExtStatixGenerateStratego")
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
