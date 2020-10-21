plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.cli")
}

languageCliProject {
  adapterProject.set(project(":stratego"))
}
