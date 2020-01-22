plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.jetbrains.intellij")
}

dependencies {
  implementation(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  implementation(project(":spoofax.core"))
  implementation(project(":spoofax.intellij"))
  implementation(project(":tiger.spoofax")) {
      exclude(group = "org.slf4j")
  }

  implementation("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}

intellij {
  version = "2019.3.2"
}

// Skip non-incremental, slow, and unecessary buildSearchableOptions task from IntelliJ.
tasks.getByName("buildSearchableOptions").onlyIf({ false })
