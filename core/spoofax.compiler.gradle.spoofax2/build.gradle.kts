plugins {
  id("org.metaborg.gradle.config.kotlin-gradle-plugin")
  kotlin("jvm")
  kotlin("kapt")
  id("org.gradle.kotlin.kotlin-dsl") // Same as `kotlin-dsl`, but without version, which is already set in root project.
}

metaborg {
  kotlinApiVersion = "1.3"
  kotlinLanguageVersion = "1.3"
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  kapt(platform(project(":spoofax.depconstraints")))

  api(project(":spoofax.compiler"))
  api(project(":spoofax.compiler.spoofax2"))
  api(project(":spoofax.compiler.spoofax2.dagger"))
  api("com.google.dagger:dagger")
  implementation("org.metaborg:pie.runtime")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  // Dependencies to be able to use/configure the extensions provided by these Gradle plugins.
  compileOnly(project(":spoofax.compiler.gradle"))
  compileOnly("org.metaborg.devenv:spoofax.gradle")
}

gradlePlugin {
  plugins {
    create("spoofax-compiler-spoofax2-language") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofax2.language"
      implementationClass = "mb.spoofax.compiler.gradle.spoofax2.plugin.Spoofax2LanguagePlugin"
    }
  }
}
