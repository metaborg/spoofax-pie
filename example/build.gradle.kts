plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.spoofax.gradle.langspec") version "0.4.6" apply false
  id("de.set.ecj") version "1.4.1" apply false
  id("org.metaborg.coronium.bundle") version "0.3.3" apply false
  id("biz.aQute.bnd.builder") version "5.1.2" apply false
  id("org.jetbrains.intellij") version "0.4.22" apply false
  id("com.palantir.graal") version "0.7.1" apply false

  id("org.metaborg.spoofax.compiler.gradle.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.compiler.gradle.cli") apply false
  id("org.metaborg.spoofax.compiler.gradle.eclipse.externaldeps") apply false
  id("org.metaborg.spoofax.compiler.gradle.eclipse") apply false
  id("org.metaborg.spoofax.compiler.gradle.intellij") apply false
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.spoofax3.language") apply false
}

subprojects {
  metaborg {
    configureSubProject()
    // Do not publish examples.
    javaCreatePublication = false
    javaCreateSourcesJar = false
    javaCreateJavadocJar = false
  }
}
