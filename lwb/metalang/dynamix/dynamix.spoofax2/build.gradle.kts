plugins {
  id("org.metaborg.devenv.spoofax.gradle.langspec")
  `maven-publish`
}

val spoofax2DevenvVersion: String by ext
spoofaxLanguageSpecification {
  addSourceDependenciesFromMetaborgYaml.set(false)
  addCompileDependenciesFromMetaborgYaml.set(false)
}
dependencies {
  compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.esv:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.template:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:statix.lang:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:sdf3.ext.statix:$spoofax2DevenvVersion")

  sourceLanguage("org.metaborg.devenv:meta.lib.spoofax:$spoofax2DevenvVersion")
  sourceLanguage("org.metaborg.devenv:statix.runtime:$spoofax2DevenvVersion")
}
