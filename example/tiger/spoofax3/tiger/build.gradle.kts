plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}
