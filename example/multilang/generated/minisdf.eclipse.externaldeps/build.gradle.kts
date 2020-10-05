plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse.externaldeps")
}

languageEclipseExternaldepsProject {
  adapterProject.set(project(":minisdf.spoofax"))
}
