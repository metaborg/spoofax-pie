import mb.spoofax.compiler.util.GradleDependency

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
    api(project(":signature"))
}

languageProject {
    shared {
        name("Module")
        defaultPackageId("mb.module")
    }
    compilerInput {
        withMultilangAnalyzer().run {
            rootModules(listOf("modules/modules"))
        }
    }
    statixDependencies.set(listOf(project(":signature")))
}

spoofax2BasedLanguageProject {
    compilerInput {
        withMultilangAnalyzer()
        project
            .languageSpecificationDependency(GradleDependency.project(":module-interface.spoofaxcore"))
    }
}

languageAdapterProject {
    compilerInput {
        project.compositionGroup("minimeta")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
