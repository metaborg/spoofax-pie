plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  adapterProject.set(project(":libstatix"))
  compilerInput {
    languageGroup("mb.spoofax.lwb")
  }
}
