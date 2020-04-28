plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.2"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.gradle.config.kotlin-gradle-plugin") version "0.3.21" apply false
  id("org.metaborg.coronium.bundle") version "0.2.2" apply false
  id("org.metaborg.coronium.embedding") version "0.2.2" apply false
  id("org.metaborg.spoofax.gradle.langspec") version "0.2.1" apply false
  id("org.metaborg.spoofax.gradle.project") version "0.2.1" apply false
  id("net.ltgt.apt") version "0.21" apply false
  id("net.ltgt.apt-idea") version "0.21" apply false
  id("de.set.ecj") version "1.4.1" apply false
  id("biz.aQute.bnd.builder") version "4.3.1" apply false
  id("com.palantir.graal") version "0.6.0" apply false
  id("org.jetbrains.intellij") version "0.4.15" apply false
  kotlin("jvm") version "1.3.41" // Use 1.3.41 to keep in sync with embedded Kotlin version of Gradle 5.6.4.
  `kotlin-dsl`
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

gitonium {
  // Disable snapshot dependency checks for releases, until we depend on a stable version of MetaBorg artifacts.
  checkSnapshotDependenciesInRelease = false
}
