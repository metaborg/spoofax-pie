plugins {
  id("org.metaborg.devenv.spoofax.gradle.langspec")
  `maven-publish`
}

val spoofax2DevenvVersion: String by ext
dependencies {
  compileLanguage("org.metaborg.devenv:statix.lang:$spoofax2DevenvVersion")
}
