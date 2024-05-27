import mb.spoofax.compiler.util.GradleDependency

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    testImplementation(compositeBuild("spoofax.test"))
    testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
    shared {
        name("Mod")
        defaultPackageId("mb.mod")
    }
    compilerInput {
        withParser().run {
            startSymbol("Start")
        }
        withStyler()
        withConstraintAnalyzer().run {
            enableNaBL2(false)
            enableStatix(true)
            multiFile(true)
        }
        withStrategoRuntime()
    }
}

spoofax2BasedLanguageProject {
    compilerInput {
        withParser()
        withStyler()
        withConstraintAnalyzer().run {
            copyStatix(true)
        }
        withStrategoRuntime().run {
            copyCtree(true)
            copyClasses(false)
        }
        project
            .languageSpecificationDependency(GradleDependency.project(":mod.spoofaxcore"))
    }
}
