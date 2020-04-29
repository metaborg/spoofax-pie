plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

spoofaxEclipseProject {
  eclipseExternaldepsProject.set(project(":mod.eclipse.externaldeps"))
  adapterProject.set(project(":mod.spoofax"))
}
