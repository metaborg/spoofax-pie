import mb.spoofax.compiler.util.GradleDependency

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    testImplementation(platform(libs.metaborg.platform))
    testImplementation(libs.spoofax3.test)
    testImplementation(libs.junit)
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
