plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  // Platforms
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))
  testAnnotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  // Main
  api(project(":tiger"))
  api(compositeBuild("spoofax.core"))
  api(compositeBuild("jsglr1.pie"))
  api(compositeBuild("constraint.pie"))
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")

  // Test
  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}
