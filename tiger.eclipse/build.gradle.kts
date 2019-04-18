plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

bundle {
  bundleApiProject(":spoofax.eclipse.externaldeps")
  bundleApiProject(":spoofax.eclipse")
  bundleApiProject(":tiger.eclipse.externaldeps")
}

dependencies {
  api(platform(project(":depconstraints")))

  compileOnly("org.checkerframework:checker-qual-android")
}
