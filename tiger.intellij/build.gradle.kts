plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.jetbrains.intellij") version "0.4.8"
}

dependencies {
  implementation(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  implementation(project(":spoofax.core"))
  implementation(project(":spoofax.intellij"))
  implementation(project(":tiger.spoofax"))

  implementation("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}

intellij {
  version = "2019.1.1"
}

// Skip non-incremental, slow, and unecessary buildSearchableOptions task from IntelliJ.
tasks.getByName("buildSearchableOptions").onlyIf({ false })
