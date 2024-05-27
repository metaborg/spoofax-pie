import mb.spoofax.compiler.util.GradleDependency

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
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
