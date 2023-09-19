plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")

  api("org.metaborg.devenv:statix.solver")
  api("com.google.guava:guava:27.0")

  implementation(project(":stratego.common"))
  implementation(project(":jsglr.common"))

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")

  annotationProcessor("org.immutables:value")
}
