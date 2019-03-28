plugins {
  id("org.metaborg.gradle.config.java-library")
}

val spoofaxVersion = extra["spoofaxVersion"] as String

dependencies {
  api("org.metaborg:org.spoofax.terms:$spoofaxVersion")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
