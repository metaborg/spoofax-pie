plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":common"))
  api(project(":jsglr.common"))
  api(project(":stratego.common"))

  api("org.metaborg:log.api")

  implementation("org.metaborg:nabl2.terms")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.derive4j:derive4j")
}
