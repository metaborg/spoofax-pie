plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("common"))
  api(compositeBuild("spoofax.core"))
  api(compositeBuild("spoofax.compiler"))
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.task.archive")

  api(project(":sdf3"))
  api(project(":stratego"))
  api(project(":esv"))
  api(project(":statix"))

  api(project(":libspoofax2"))
  api(project(":libstatix"))

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}
