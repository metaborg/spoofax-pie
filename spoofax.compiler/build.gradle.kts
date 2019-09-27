plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":depconstraints")))
  annotationProcessor(platform(project(":depconstraints")))

  api(project(":common"))

  api("com.samskivert:jmustache:1.15")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}

tasks.test {
  testLogging {
    showStandardStreams = true
  }
}
