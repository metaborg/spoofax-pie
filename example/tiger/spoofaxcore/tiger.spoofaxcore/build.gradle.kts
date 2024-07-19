plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
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
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.nabl2.lang)
    compileLanguage(libs.spoofax2.nabl2.extdynsem)
    compileLanguage(libs.dynsem)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.nabl2.shared)
    sourceLanguage(libs.nabl2.runtime)

    compileOnly(libs.spoofax.core)
}
