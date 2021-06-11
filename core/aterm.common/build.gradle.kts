plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")

  api("org.metaborg.devenv:org.spoofax.terms")

  // Depend on Stratego projects for access to standard library strategies which do aterm pretty printing.
  implementation("org.metaborg.devenv:org.spoofax.interpreter.core")
  implementation("org.metaborg.devenv:org.strategoxt.strj")

  compileOnly("org.checkerframework:checker-qual-android")
}
