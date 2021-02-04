plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.4"
  id("org.metaborg.gitonium") version "0.1.4"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  kotlin("jvm") version "1.3.41" apply false // Use 1.3.41 to keep in sync with embedded Kotlin version of Gradle 5.6.4.
  `kotlin-dsl` apply false

  id("org.metaborg.devenv.spoofax.gradle.langspec") version "0.1.0" apply false
  id("de.set.ecj") version "1.4.1" apply false
  id("org.metaborg.coronium.bundle") version "0.3.6" apply false
  id("biz.aQute.bnd.builder") version "5.2.0" apply false
  id("org.jetbrains.intellij") version "0.6.5" apply false

  id("org.metaborg.spoofax.compiler.gradle.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.compiler.gradle.cli") apply false
  id("org.metaborg.spoofax.compiler.gradle.eclipse") apply false
  id("org.metaborg.spoofax.compiler.gradle.intellij") apply false
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language") apply false
}

subprojects {
  metaborg {
    configureSubProject()
    if(name.contains(".cli") || name.contains(".eclipse") || name.contains(".intellij")) {
      // Do not publish CLI, Eclipse plugin, and IntelliJ plugin for now.
      javaCreatePublication = false
      javaCreateSourcesJar = false
      javaCreateJavadocJar = false
    }
  }
}

val spoofax2Version: String = System.getProperty("spoofax2Version")
val spoofax2DevenvVersion: String = System.getProperty("spoofax2DevenvVersion")
allprojects {
  ext["spoofax2Version"] = spoofax2Version
  ext["spoofax2DevenvVersion"] = spoofax2DevenvVersion
}
