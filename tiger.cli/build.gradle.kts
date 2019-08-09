plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.tiger.cli.Main"
}

dependencies {
  implementation(platform(project(":depconstraints")))

  implementation(project(":tiger.spoofax"))
  implementation(project(":spoofax.cli"))
  implementation("org.metaborg:log.backend.slf4j")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.dagger")

  implementation("org.slf4j:slf4j-simple:1.7.26")

  compileOnly("org.checkerframework:checker-qual-android")
}
