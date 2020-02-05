plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":common"))

  api("org.metaborg:log.api")
  api("org.metaborg:resource")

  api("org.metaborg:org.spoofax.terms")
  api("org.metaborg:org.spoofax.interpreter.core")
  api("org.metaborg:org.strategoxt.strj")

  // For some reason, Gradle ignores transitive dependencies of 'org.strategoxt.strj', so we add them here...
  implementation("org.metaborg:org.spoofax.interpreter.library.java")
  implementation("org.metaborg:org.spoofax.interpreter.library.jsglr")
  implementation("org.metaborg:org.spoofax.interpreter.library.xml")

  compileOnly("org.checkerframework:checker-qual-android")
}
