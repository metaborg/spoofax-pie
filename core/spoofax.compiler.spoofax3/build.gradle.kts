plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":common"))
  api(project(":spoofax.core"))
  api(project(":spoofax.compiler"))
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("com.samskivert:jmustache:1.15")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")

  annotationProcessor("org.immutables:value")
}
