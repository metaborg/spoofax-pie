plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api(project(":jsglr.common"))
  api(project(":stratego.common"))
  api(project(":spoofax.core"))

  api("org.metaborg.devenv:statix.solver")
  api("org.metaborg:pie.api")
  api("org.yaml:snakeyaml")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")

  annotationProcessor("com.google.dagger:dagger-compiler")
  annotationProcessor("org.immutables:value")
}
