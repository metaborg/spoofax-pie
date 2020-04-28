plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps")
}

spoofaxEclipseExternaldepsProject {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.EclipseExternaldepsProjectSettings(
    adapterGradleProject = project(":tiger.spoofax")
  ))
}
