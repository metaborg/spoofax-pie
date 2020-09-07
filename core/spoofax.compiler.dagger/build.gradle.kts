plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":spoofax.compiler"))
  api("com.google.dagger:dagger")

  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  annotationProcessor("com.google.dagger:dagger-compiler")
}
