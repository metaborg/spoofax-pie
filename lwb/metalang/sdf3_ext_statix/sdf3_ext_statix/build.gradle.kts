import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

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
val spoofax2DevenvVersion = "2.6.0-SNAPSHOT"  // TODO
spoofax2BasedLanguageProject {
    compilerInput {
        withStrategoRuntime().run {
            copyClasses(true)
        }
        project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:sdf3.ext.statix:$spoofax2DevenvVersion"))
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
