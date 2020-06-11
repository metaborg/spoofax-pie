plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":common"))

  api("org.metaborg:statix.solver")
  api("org.metaborg:pie.api")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")

  annotationProcessor("org.immutables:value")

  testAnnotationProcessor(platform(project(":spoofax.depconstraints")))
}
