plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.4"
  id("org.metaborg.gitonium") version "0.1.4"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.gradle.config.kotlin-gradle-plugin") version "0.4.4" apply false
  id("org.metaborg.coronium.bundle") version "0.3.6" apply false
  id("net.ltgt.apt") version "0.21" apply false
  id("net.ltgt.apt-idea") version "0.21" apply false
  id("biz.aQute.bnd.builder") version "5.2.0" apply false
  id("org.jetbrains.intellij") version "0.6.5" apply false
  kotlin("jvm") version "1.3.41" apply false // Use 1.3.41 to keep in sync with embedded Kotlin version of Gradle 5.6.4.
  `kotlin-dsl` apply false
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

val spoofax2Version: String = System.getProperty("spoofax2Version")
val spoofax2DevenvVersion: String = System.getProperty("spoofax2DevenvVersion")
allprojects {
  ext["spoofax2Version"] = spoofax2Version
  ext["spoofax2DevenvVersion"] = spoofax2DevenvVersion
}
