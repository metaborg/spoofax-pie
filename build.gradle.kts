plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.6"
  id("org.metaborg.gitonium") version "0.1.1"
  id("org.metaborg.coronium.bundle") version "0.1.0" apply false // Only apply in subprojects
  id("net.ltgt.apt") version "0.21" apply false // Only apply in subprojects
  id("net.ltgt.apt-idea") version "0.21" apply false // Only apply in subprojects
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
