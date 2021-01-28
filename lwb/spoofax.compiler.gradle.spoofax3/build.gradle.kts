plugins {
  id("org.metaborg.gradle.config.kotlin-gradle-plugin")
  id("org.metaborg.gitonium")
  kotlin("jvm")
  kotlin("kapt")
  id("org.gradle.kotlin.kotlin-dsl") // Same as `kotlin-dsl`, but without version, which is already set in root project.
}

metaborg {
  kotlinApiVersion = "1.3"
  kotlinLanguageVersion = "1.3"
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  kapt(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("spoofax.compiler"))
  api(compositeBuild("spoofax.compiler.dagger"))
  api(project(":spoofax.compiler.spoofax3"))
  api(project(":spoofax.compiler.spoofax3.dagger"))
  api("com.google.dagger:dagger")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:log.backend.slf4j")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  // Dependencies to be able to use/configure the extensions provided by these Gradle plugins.
  compileOnly(compositeBuild("spoofax.compiler.gradle"))
}

gradlePlugin {
  plugins {
    create("spoofax-compiler-spoofax3-language") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofax3.language"
      implementationClass = "mb.spoofax.compiler.gradle.spoofax3.plugin.Spoofax3LanguagePlugin"
    }
  }
}
