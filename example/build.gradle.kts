plugins {
  id("org.metaborg.gradle.config.root-project") version "0.5.6"
  id("org.metaborg.gitonium") version "1.2.0"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.devenv.spoofax.gradle.langspec") version "0.1.32" apply false
  id("org.metaborg.coronium.bundle") version "0.3.17" apply false
  id("biz.aQute.bnd.builder") version "5.3.0" apply false
  id("org.jetbrains.intellij") version "1.4.0" apply false
  id("com.palantir.graal") version "0.7.1" apply false

  id("org.metaborg.spoofax.compiler.gradle.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.compiler.gradle.cli") apply false
  id("org.metaborg.spoofax.compiler.gradle.eclipse") apply false
  id("org.metaborg.spoofax.compiler.gradle.intellij") apply false
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language") apply false

  id("org.metaborg.spoofax.lwb.compiler.gradle.language") apply false
}

subprojects {
  metaborg {
    configureSubProject()
    // Do not publish examples, except for `tiger.spoofax3`
    if(this@subprojects.name != "tiger.spoofax3") {
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
