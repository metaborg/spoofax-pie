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

  api(project(":spoofax.lwb.compiler"))
  api(project(":spoofax.lwb.compiler.dagger"))
  api("com.google.dagger:dagger")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:log.backend.slf4j")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.
}

gradlePlugin {
  plugins {
    create("spoofax-lwb-compiler-language") {
      id = "org.metaborg.spoofax.lwb.compiler.gradle.language"
      implementationClass = "mb.spoofax.lwb.compiler.gradle.LanguagePlugin"
    }
  }
}
