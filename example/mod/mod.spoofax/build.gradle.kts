import mb.spoofax.compiler.adapter.AdapterProjectCompiler

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(compositeBuild("spoofax2.common"))
}

languageAdapterProject {
    languageProject.set(project(":mod"))
    compilerInput {
        withParser()
        withStyler()
        withStrategoRuntime()
        withConstraintAnalyzer()
        project.configureCompilerInput()
    }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {

}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
