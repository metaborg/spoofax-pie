import mb.spoofax.compiler.gradle.spoofaxcore.*

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
}

spoofaxCliProject {
  settings.set(CliProjectSettings(
    adapterGradleProject = project(":sdf3.spoofax")
  ))
}
