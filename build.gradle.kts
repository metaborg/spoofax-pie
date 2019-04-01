plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.6"
  id("org.metaborg.gitonium") version "0.1.1"
  id("net.ltgt.apt") version "0.21" apply false // Only apply in subprojects
  id("net.ltgt.apt-idea") version "0.21" apply false // Only apply in subprojects
}

subprojects {
  metaborg {
    configureSubProject()
  }
  extra["daggerVersion"] = "2.21"
  extra["spoofaxVersion"] = "2.5.1"
  extra["pieVersion"] = "0.4.2"
}

gitonium {
  // Disable snapshot dependency checks for releases, until we depend on a stable version of MetaBorg artifacts.
  checkSnapshotDependenciesInRelease = false
}
