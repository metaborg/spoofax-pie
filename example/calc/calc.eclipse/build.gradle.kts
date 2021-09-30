plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

mavenize {
  majorVersion.set("2021-03")
}

languageEclipseProject {
  adapterProject.set(project(":calc"))
}
