plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  jacoco
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))
  testAnnotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api("org.metaborg:statix.common")
  api("org.metaborg:tego.runtime")
  api("org.metaborg:log.api")

  // api("com.google.guava:guava:27.0")
  // api("com.google.guava:failureaccess:1.0.1")

  api("org.metaborg.devenv:statix.solver")
  api("org.metaborg.devenv:statix.generator")

  implementation(project(":stratego.common"))
  implementation(project(":jsglr.common"))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("org.immutables:value")
  testAnnotationProcessor("org.immutables:value")

  testCompileOnly("org.checkerframework:checker-qual-android")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.10")
  testCompileOnly("org.immutables:value")
  testCompileOnly("javax.annotation:javax.annotation-api")

  testImplementation("com.opencsv:opencsv:4.1")

  // Immutables
  testCompileOnly("org.immutables:value")
  testAnnotationProcessor("org.immutables:value")
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
  dependsOn(tasks.test)
}

//tasks { withType<Test> {
//  debug = true
//  maxHeapSize = "3g"
//} }
