plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

bundle {
  bundleApiProject(":spoofax.eclipse")
}

dependencies {
  api(platform(project(":depconstraints")))

  compileOnly("org.checkerframework:checker-qual-android")
}
