plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

bundle {
  requireTargetPlatform("javax.inject")
  requireTargetPlatform("org.eclipse.core.runtime")
  requireTargetPlatform("org.eclipse.core.expressions")
  requireTargetPlatform("org.eclipse.core.resources")
  requireTargetPlatform("org.eclipse.core.filesystem")
  requireTargetPlatform("org.eclipse.ui")
  requireTargetPlatform("org.eclipse.ui.views")
  requireTargetPlatform("org.eclipse.ui.editors")
  requireTargetPlatform("org.eclipse.ui.console")
  requireTargetPlatform("org.eclipse.ui.workbench")
  requireTargetPlatform("org.eclipse.ui.workbench.texteditor")
  requireTargetPlatform("org.eclipse.ui.ide")
  requireTargetPlatform("org.eclipse.jface.text")
  requireTargetPlatform("org.eclipse.swt")
  requireTargetPlatform("com.ibm.icu")

  requireEmbeddingBundle(":spoofax.eclipse.externaldeps")
}

dependencies {
  // Dependency constraints.
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  // Compile-time annotations.
  compileOnly("org.checkerframework:checker-qual-android")

  // Annotation processors.
  annotationProcessor("com.google.dagger:dagger-compiler")
}
