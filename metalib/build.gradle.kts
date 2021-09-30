plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.6"
  id("org.metaborg.gitonium") version "0.1.4"

  id("org.metaborg.devenv.spoofax.gradle.langspec") version "0.1.4" apply false
  id("org.metaborg.coronium.bundle") version "0.3.11" apply false
  id("biz.aQute.bnd.builder") version "5.3.0" apply false

  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.compiler.gradle.eclipse") apply false
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
