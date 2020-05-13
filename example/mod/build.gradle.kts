plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.2"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.spoofax.gradle.langspec") version "0.2.4" apply false
  id("de.set.ecj") version "1.4.1" apply false
  id("org.metaborg.coronium.bundle") version "0.3.0" apply false
  id("biz.aQute.bnd.builder") version "5.0.1" apply false
  id("org.jetbrains.intellij") version "0.4.21" apply false
}

subprojects {
  metaborg {
    configureSubProject()
  }
}
