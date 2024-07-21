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

    compileLanguage(libs.esv.lang)
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.nabl2.lang)
    compileLanguage(libs.statix.lang)
    compileLanguage(libs.spoofax2.nabl2.extdynsem)
    compileLanguage(libs.spoofax2.dynsem.lang)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.nabl2.shared)
    sourceLanguage(libs.nabl2.runtime)
    sourceLanguage(libs.statix.runtime)

    compileOnly(libs.spoofax.core)
}
