import mb.spoofax.compiler.util.GradleDependency

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
