plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))

  api("org.metaborg:log.api")
  api("org.metaborg:resource")

  api("org.metaborg:org.spoofax.terms")
  api("org.metaborg:org.spoofax.interpreter.core")
  api("org.metaborg:org.strategoxt.strj")

  compileOnly("org.checkerframework:checker-qual-android")
}
