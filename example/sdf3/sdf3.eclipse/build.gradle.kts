import mb.spoofax.compiler.gradle.spoofaxcore.*

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

spoofaxEclipseProject {
  settings.set(EclipseProjectSettings(
    eclipseExternaldepsGradleProject = project(":sdf3.eclipse.externaldeps"),
    adapterGradleProject = project(":sdf3.spoofax")
  ))
}
