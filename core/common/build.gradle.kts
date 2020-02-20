plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))
  testImplementation(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:resource")

  compileOnly("org.derive4j:derive4j-annotation")
  compileOnly("org.checkerframework:checker-qual-android")

  testCompileOnly("org.checkerframework:checker-qual-android")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")

  annotationProcessor("org.derive4j:derive4j")
}
