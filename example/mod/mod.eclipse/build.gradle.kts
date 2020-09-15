plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  eclipseExternaldepsProject.set(project(":mod.eclipse.externaldeps"))
  adapterProject.set(project(":mod.spoofax"))
}
