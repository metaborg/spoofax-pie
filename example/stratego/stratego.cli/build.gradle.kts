plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
}

spoofaxCliProject {
  adapterProject.set(project(":stratego.spoofax"))
}
