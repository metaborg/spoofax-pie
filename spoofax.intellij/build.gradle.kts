plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.jetbrains.intellij")
}

dependencies {
  implementation(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  api(project(":spoofax.core"))

  api("org.metaborg:log.api")
  implementation("org.metaborg:log.backend.noop")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.dagger")
  implementation("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}

intellij {
  version = "2019.3.2"
}

// Skip non-incremental, slow, and unnecessary buildSearchableOptions task from IntelliJ.
tasks.getByName("buildSearchableOptions").onlyIf { false }
