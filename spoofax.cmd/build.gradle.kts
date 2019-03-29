plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  implementation(project(":spoofax.core"))
  implementation("info.picocli:picocli:3.9.5")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
