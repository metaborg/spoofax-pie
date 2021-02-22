plugins {
  id("org.metaborg.gradle.config.java-library")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api(project(":completions.common"))
  api("org.metaborg:log.api")
  api("org.metaborg:log.dagger")
  api("org.metaborg:resource")
  api("org.metaborg:resource.dagger")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")
  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}
