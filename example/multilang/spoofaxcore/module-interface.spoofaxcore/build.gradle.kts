plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    compileLanguage(libs.statix.lang)
    sourceLanguage(project(":signature-interface.spoofaxcore"))

    compileOnly(libs.spoofax.core)
}

afterEvaluate {
    val importSignature = tasks.register<Sync>("importSignature") {
        from("../signature-interface.spoofaxcore/trans/abstract-sig")
        into("trans/abstract-sig")
    }
    tasks.getByName("spoofaxBuildLanguageSpec") {
        dependsOn(importSignature)
    }
}
