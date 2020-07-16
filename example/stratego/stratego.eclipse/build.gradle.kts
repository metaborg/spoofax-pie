plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

spoofaxEclipseProject {
  eclipseExternaldepsProject.set(project(":stratego.eclipse.externaldeps"))
  adapterProject.set(project(":stratego.spoofax"))
}
