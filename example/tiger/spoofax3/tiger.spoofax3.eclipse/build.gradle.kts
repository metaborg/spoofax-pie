plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  adapterProject.set(project(":tiger.spoofax3"))
}
mavenize {
  majorVersion.set("2021-03")
}