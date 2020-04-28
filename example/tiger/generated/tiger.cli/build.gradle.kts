plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
}

spoofaxCliProject {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.CliProjectSettings(
    adapterGradleProject = project(":tiger.spoofax")
  ))
}
