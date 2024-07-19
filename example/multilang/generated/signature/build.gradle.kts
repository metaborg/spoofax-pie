import mb.spoofax.compiler.util.GradleDependency

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
    id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageProject {
    shared {
        name("Signature")
        defaultPackageId("mb.signature")
    }
    compilerInput {
        withMultilangAnalyzer().run {
            rootModules(listOf("abstract-sig/conflicts/sorts", "abstract-sig/conflicts/constructors"))
        }
    }
}

spoofax2BasedLanguageProject {
    compilerInput {
        withMultilangAnalyzer()
        project.languageSpecificationDependency(GradleDependency.project(":signature-interface.spoofaxcore"))
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
