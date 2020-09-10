// This is a separate project with Dagger components/modules, because the Dagger annotation processor cannot run on the
// main project, as there are staging conflicts with the other (org.immutables/derive4j) annotation processors.

plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api(project(":spoofax.compiler.spoofax2"))
  api(project(":spoofax.compiler.dagger"))
  api("com.google.dagger:dagger")

  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  annotationProcessor("com.google.dagger:dagger-compiler")
}
