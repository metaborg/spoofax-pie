import org.metaborg.core.language.*

plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

val spoofax2DevenvVersion: String by ext
spoofaxLanguageSpecification {
    addSourceDependenciesFromMetaborgYaml.set(false)
    addCompileDependenciesFromMetaborgYaml.set(false)
    addLanguageContributionsFromMetaborgYaml.set(false)
    languageContributions.add(LanguageContributionIdentifier(LanguageIdentifier("$group", "org.metaborg.meta.lang.template", LanguageVersion.parse("$version")), "TemplateLang"))
}
dependencies {
    compileLanguage(libs.esv.lang)
    sourceLanguage(libs.sdf3.lang)
    sourceLanguage(libs.statix.lang)

    sourceLanguage(libs.spoofax2.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)

    sourceLanguage(project(":dynamix.spoofax2"))
}
