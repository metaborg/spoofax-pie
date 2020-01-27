plugins {
  id("org.metaborg.gradle.config.java-library")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}
dependencies {
  api(platform("$group:spoofax.depconstraints:$version"))
  annotationProcessor(platform("$group:spoofax.depconstraints:$version"))

  api(project(":tiger"))
  api("$group:spoofax.core:$version")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}
