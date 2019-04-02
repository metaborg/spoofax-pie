plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.tiger.cmd.Main"
}

val spoofaxVersion = extra["spoofaxVersion"] as String
val pieVersion = extra["pieVersion"] as String

dependencies {
  implementation(project(":tiger.spoofax"))
  implementation(project(":spoofax.cmd"))
  implementation("org.metaborg:pie.runtime:$pieVersion")
  implementation("org.metaborg:pie.dagger:$pieVersion")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
