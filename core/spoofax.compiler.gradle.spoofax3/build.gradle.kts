plugins {
  id("org.metaborg.gradle.config.kotlin-gradle-plugin")
  id("org.metaborg.gitonium")
  kotlin("jvm")
  kotlin("kapt")
  id("org.gradle.kotlin.kotlin-dsl") // Same as `kotlin-dsl`, but without version, which is already set in root project.
}

metaborg {
  kotlinApiVersion = "1.2"
  kotlinLanguageVersion = "1.2"
}

repositories {
  gradlePluginPortal() // Gradle plugin portal as repository for regular dependencies, as we depend on Gradle plugins.
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  kapt(platform(project(":spoofax.depconstraints")))

  api(project(":spoofax.compiler"))
  api(project(":spoofax.compiler.dagger"))
  api("com.google.dagger:dagger")

  implementation("org.metaborg:pie.runtime")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  // Dependencies to be able to configure the extensions provided by these Gradle plugins.
  compileOnly("org.metaborg:coronium:0.3.0")
  compileOnly("biz.aQute.bnd:biz.aQute.bnd.gradle:5.0.1")
  compileOnly("gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.4.21")
}
