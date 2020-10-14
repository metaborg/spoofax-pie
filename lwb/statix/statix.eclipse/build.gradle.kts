plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

dependencies {
  api(platform("org.metaborg:spoofax.depconstraints:$version")) // TODO: why is this needed? the Gradle plugin should add this.
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

languageEclipseProject {
  eclipseExternaldepsProject.set(project(":statix.eclipse.externaldeps"))
  adapterProject.set(project(":statix.spoofax"))
}
