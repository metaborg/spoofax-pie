plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps")
}

spoofaxEclipseExternaldepsProject {
  adapterProject.set(project(":stratego.spoofax"))
}
