import org.metaborg.core.language.*

plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

spoofaxLanguageSpecification {
    addSourceDependenciesFromMetaborgYaml.set(false)
    addCompileDependenciesFromMetaborgYaml.set(false)
    addLanguageContributionsFromMetaborgYaml.set(false)
    languageContributions.add(LanguageContributionIdentifier(LanguageIdentifier("$group", "org.metaborg.meta.lang.template", LanguageVersion.parse("$version")), "TemplateLang"))

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    compileLanguage(libs.esv.lang)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.sdf3.lang)
    sourceLanguage(libs.spoofax2.meta.lib.analysis)
    sourceLanguage(libs.nabl2.lang)
    sourceLanguage(libs.nabl2.runtime)
    sourceLanguage(libs.statix.lang)
    sourceLanguage(libs.statix.runtime)
    sourceLanguage(project(":dynamix.spoofax2"))

    compileOnly(libs.spoofax.core)
}
