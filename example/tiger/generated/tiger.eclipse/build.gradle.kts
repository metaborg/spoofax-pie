plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

spoofaxEclipseProject {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.EclipseProjectSettings(
    eclipseExternaldepsGradleProject = project(":tiger.eclipse.externaldeps"),
    adapterGradleProject = project(":tiger.spoofax")
  ))
}
