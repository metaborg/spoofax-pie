plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  eclipseExternaldepsProject.set(project(":ministr.eclipse.externaldeps"))
  adapterProject.set(project(":ministr.spoofax"))
}
