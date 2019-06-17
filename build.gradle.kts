plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.8"
  id("org.metaborg.gitonium") version "0.1.2"

  id("org.metaborg.coronium.bundle") version "0.1.2" apply false // Only apply in subprojects
  id("org.metaborg.spoofax.gradle.langspec") version "develop-SNAPSHOT" apply false // Only apply in subprojects
  id("net.ltgt.apt") version "0.21" apply false // Only apply in subprojects
  id("net.ltgt.apt-idea") version "0.21" apply false // Only apply in subprojects
  id("de.set.ecj") version "1.4.1" apply false // Only apply in subprojects
  id("biz.aQute.bnd.builder") version "4.1.0" apply false // Only apply in subprojects
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

allprojects {
  repositories {
    // Required by NaBL2/Statix solver.
    maven("http://nexus.usethesource.io/content/repositories/public/")
  }
}

gitonium {
  // Disable snapshot dependency checks for releases, until we depend on a stable version of MetaBorg artifacts.
  checkSnapshotDependenciesInRelease = false
}
