plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  implementation(project(":spoofax.core"))
  implementation("info.picocli:picocli:3.9.5")

  compileOnly("org.checkerframework:checker-qual-android")
}
