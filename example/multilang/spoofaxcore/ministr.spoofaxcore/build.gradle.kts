plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

spoofaxLanguageSpecification {
    addSourceDependenciesFromMetaborgYaml.set(false)
    addCompileDependenciesFromMetaborgYaml.set(false)

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.statix.lang)
    compileLanguage(libs.sdf3.extstatix)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)
    sourceLanguage(project(":signature-interface.spoofaxcore"))
    sourceLanguage(project(":module-interface.spoofaxcore"))

    compileOnly(libs.spoofax.core)
}

afterEvaluate {
    val importSignature = tasks.register<Sync>("importSignature") {
        from("../signature-interface.spoofaxcore/trans/abstract-sig")
        into("trans/abstract-sig")
    }
    val importModule = tasks.register<Sync>("importModule") {
        from("../module-interface.spoofaxcore/trans/modules")
        into("trans/modules")
    }
    tasks.getByName("spoofaxBuildLanguageSpec") {
        dependsOn(importSignature)
        dependsOn(importModule)
    }
}
