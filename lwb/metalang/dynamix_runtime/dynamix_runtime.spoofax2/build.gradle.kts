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
  compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.esv:$spoofax2DevenvVersion")

  sourceLanguage("org.metaborg.devenv:meta.lib.spoofax:$spoofax2DevenvVersion")

  sourceLanguage(project(":dynamix.spoofax2"))
  sourceLanguage(project(":tim.spoofax2"))
}
