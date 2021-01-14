plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")

  api("org.metaborg:log.api")
  api("org.metaborg:resource")

  api("org.metaborg.devenv:org.spoofax.terms")
  api("org.metaborg.devenv:org.spoofax.interpreter.core")
  api("org.metaborg.devenv:org.strategoxt.strj")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.derive4j:derive4j")
}
