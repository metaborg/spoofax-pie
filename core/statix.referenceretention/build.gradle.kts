plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.kotlin-library")
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
  api("org.metaborg:statix.codecompletion")

  api("org.metaborg.devenv:statix.solver")
  api("org.metaborg.devenv:statix.generator")

  implementation(project(":stratego.common"))
  implementation(project(":jsglr.common"))

  compileOnly("org.checkerframework:checker-qual-android")

  // Annotation processing
  api("com.google.dagger:dagger")
  annotationProcessor("com.google.dagger:dagger-compiler")
  annotationProcessor("org.immutables:value")
  annotationProcessor("org.immutables:serial")
  testAnnotationProcessor("org.immutables:value")
  testAnnotationProcessor("org.immutables:serial")
  compileOnly("org.immutables:value")
  compileOnly("org.immutables:serial")
  compileOnly("javax.annotation:javax.annotation-api")
  testCompileOnly("org.immutables:value")
  testCompileOnly("org.immutables:serial")
  testCompileOnly("javax.annotation:javax.annotation-api")

  testCompileOnly("org.checkerframework:checker-qual-android")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.10")
  testCompileOnly("javax.annotation:javax.annotation-api")

  testImplementation("com.opencsv:opencsv:4.1")
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
