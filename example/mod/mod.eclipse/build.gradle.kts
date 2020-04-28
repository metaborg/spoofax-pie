import mb.spoofax.compiler.gradle.spoofaxcore.*

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

spoofaxEclipseProject {
  settings.set(EclipseProjectSettings(
    eclipseExternaldepsGradleProject = project(":mod.eclipse.externaldeps"),
    adapterGradleProject = project(":mod.spoofax")
  ))
}
