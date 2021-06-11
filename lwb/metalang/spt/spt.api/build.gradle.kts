plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
dependencies {
  api("org.metaborg:common")
  api(compositeBuild("spoofax.core"))
  api("org.metaborg:pie.api")
  compileOnly("org.checkerframework:checker-qual-android")
}
