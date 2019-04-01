plugins {
  id("org.metaborg.gradle.config.java-library")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

val daggerVersion = extra["daggerVersion"] as String
val spoofaxVersion = extra["spoofaxVersion"] as String
val pieVersion = extra["pieVersion"] as String

dependencies {
  api(project(":tiger"))
  api(project(":spoofax.core"))
  api("org.metaborg:pie.api:$pieVersion")
  api("com.google.dagger:dagger:$daggerVersion")
  annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
