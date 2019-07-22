plugins {
  id("org.metaborg.gradle.config.java-library")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  api(project(":common"))
  api("org.metaborg:log.api")
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  compileOnly("org.derive4j:derive4j-annotation")
  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
  annotationProcessor("org.derive4j:derive4j")
}
