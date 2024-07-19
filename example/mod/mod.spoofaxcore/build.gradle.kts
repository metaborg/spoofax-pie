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
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.statix.lang)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)

    compileOnly(libs.spoofax.core)
}
