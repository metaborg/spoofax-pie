plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.gradle.config.kotlin-gradle-plugin") version "0.3.21" apply false
  id("org.metaborg.coronium.bundle") version "0.3.3" apply false
  id("net.ltgt.apt") version "0.21" apply false
  id("net.ltgt.apt-idea") version "0.21" apply false
  id("biz.aQute.bnd.builder") version "5.1.1" apply false
  id("org.jetbrains.intellij") version "0.4.18" apply false
  kotlin("jvm") version "1.3.41" // Use 1.3.41 to keep in sync with embedded Kotlin version of Gradle 5.6.4.
  `kotlin-dsl`
}

subprojects {
  metaborg {
    configureSubProject()
  }
}
