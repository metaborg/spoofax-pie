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
    api(project(":module"))
    testImplementation(libs.junit)
}

languageProject {
    shared {
        name("MiniStr")
        fileExtensions(listOf("mstr"))
        defaultPackageId("mb.ministr")
    }
    compilerInput {
        withParser().run {
            startSymbol("Start")
        }
        withStyler()
        withMultilangAnalyzer().run {
            rootModules(listOf("mini-str"))
        }
        withStrategoRuntime()
    }
    statixDependencies.set(listOf(project(":module")))
}

spoofax2BasedLanguageProject {
    compilerInput {
        withParser()
        withStyler()
        withStrategoRuntime().run {
            copyCtree(true)
            copyClasses(false)
        }
        withMultilangAnalyzer()
        project
            .languageSpecificationDependency(GradleDependency.project(":ministr.spoofaxcore"))
    }
}

languageAdapterProject {
    compilerInput {
        withParser()
        withStyler()
        withStrategoRuntime()
        withMultilangAnalyzer().run {
            preAnalysisStrategy("pre-analyze")
            postAnalysisStrategy("post-analyze")
            contextId("mini-sdf-str")
            fileConstraint("mini-str!fileOk")
            projectConstraint("mini-str!projectOk")
        }
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
