plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  jacoco
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  implementation(platform(project(":spoofax.depconstraints")))
  compileOnly(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))
  testImplementation(platform(project(":spoofax.depconstraints")))
  testCompileOnly(platform(project(":spoofax.depconstraints")))
  testAnnotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api("org.metaborg:log.api")

  api("org.metaborg:log.dagger")
  api("com.google.dagger:dagger")
  annotationProcessor("com.google.dagger:dagger-compiler")

  compileOnly("org.checkerframework:checker-qual-android")
  api("org.yaml:snakeyaml")

  testCompileOnly("org.checkerframework:checker-qual-android")
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.10")
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
  dependsOn(tasks.test)
}

//tasks { withType<Test> {
//  debug = true
//} }
