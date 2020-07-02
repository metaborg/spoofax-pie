plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  bundleTargetPlatformApi(eclipse("javax.inject"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.runtime"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.expressions"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.resources"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.filesystem"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.views"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.editors"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.console"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.workbench"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.workbench.texteditor"))
  bundleTargetPlatformApi(eclipse("org.eclipse.ui.ide"))
  bundleTargetPlatformApi(eclipse("org.eclipse.jface.text"))
  bundleTargetPlatformApi(eclipse("org.eclipse.swt"))
  bundleTargetPlatformApi(eclipse("com.ibm.icu"))

  bundleTargetPlatformImplementation(eclipse("org.eclipse.ui.views.log"))

  bundleApi(project(":spoofax.eclipse.externaldeps"))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}
