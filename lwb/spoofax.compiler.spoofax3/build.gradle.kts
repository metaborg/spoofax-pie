plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("common"))
  api(compositeBuild("spoofax.core"))
  api(compositeBuild("spoofax.compiler"))
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api(project(":sdf3.spoofax"))
  api(project(":stratego.spoofax"))

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")

  annotationProcessor("org.immutables:value")
}
