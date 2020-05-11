plugins {
  id("org.metaborg.gradle.config.java-library")
  id("net.ltgt.apt")
  id("net.ltgt.apt-idea")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))

  api("com.google.dagger:dagger")
  implementation(project(":spoofax.core"))
  api("info.picocli:picocli")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
  annotationProcessor("info.picocli:picocli-codegen")
}

tasks.compileJava {
  options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}
