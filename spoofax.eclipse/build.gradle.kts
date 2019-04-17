plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

bundle {
  bundleApiTargetPlatform("org.eclipse.core.runtime")
  bundleApiTargetPlatform("org.eclipse.core.expressions")
  bundleApiTargetPlatform("org.eclipse.core.resources")
  bundleApiTargetPlatform("org.eclipse.core.filesystem")
  bundleApiTargetPlatform("org.eclipse.ui")
  bundleApiTargetPlatform("org.eclipse.ui.views")
  bundleApiTargetPlatform("org.eclipse.ui.editors")
  bundleApiTargetPlatform("org.eclipse.ui.console")
  bundleApiTargetPlatform("org.eclipse.ui.workbench")
  bundleApiTargetPlatform("org.eclipse.ui.workbench.texteditor")
  bundleApiTargetPlatform("org.eclipse.ui.ide")
  bundleApiTargetPlatform("org.eclipse.jface.text")
  bundleApiTargetPlatform("org.eclipse.swt")
  bundleApiTargetPlatform("com.ibm.icu")

  bundleApiProvidedProject(":spoofax.eclipse.externaldeps")
}

dependencies {
  api(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}
