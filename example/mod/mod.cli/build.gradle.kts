plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
}

spoofaxCliProject {
  adapterProject.set(project(":mod.spoofax"))
}
