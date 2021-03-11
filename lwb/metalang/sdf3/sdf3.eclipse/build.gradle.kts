plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  adapterProject.set(project(":sdf3"))
  compilerInput {
    languageGroup("mb.spoofax.lwb")
  }
}
