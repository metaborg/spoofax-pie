import mb.spoofax.compiler.gradle.spoofaxcore.*

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps")
}

spoofaxEclipseExternaldepsProject {
  settings.set(EclipseExternaldepsProjectSettings(
    adapterGradleProject = project(":mod.spoofax")
  ))
}
