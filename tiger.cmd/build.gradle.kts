plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
