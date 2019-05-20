plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))
  api(project(":jsglr.common"))
  api(project(":stratego.common"))

  api("org.metaborg:log.api")
  api("org.metaborg:nabl2.solver")
  api("org.metaborg:statix.solver")

  compileOnly("org.checkerframework:checker-qual-android")
}
