plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  bundleTargetPlatformApi(eclipse("javax.inject"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.runtime"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.expressions"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.resources"))
  bundleTargetPlatformApi(eclipse("org.eclipse.core.filesystem"))
  bundleTargetPlatformApi(project(":spoofax.eclipse"))

  bundleApi(project(":statix.multilang.eclipse.externaldeps"))

  compileOnly("org.checkerframework:checker-qual-android")
}
