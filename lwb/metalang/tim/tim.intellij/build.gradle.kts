plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.intellij")
}

languageIntellijProject {
  adapterProject.set(project(":tim"))
}
