plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  eclipseExternaldepsProject.set(project(":sdf3.eclipse.externaldeps"))
  adapterProject.set(project(":sdf3.spoofax"))
}
