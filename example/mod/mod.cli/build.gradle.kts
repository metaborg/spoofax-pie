plugins {
  id("org.metaborg.gradle.config.java-application")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
}

spoofaxCliProject {
  adapterProject.set(project(":mod.spoofax"))
}
