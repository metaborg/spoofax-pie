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
  implementation("org.metaborg:log.backend.slf4j")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  // Dependencies to be able to configure the extensions provided by these Gradle plugins.
  compileOnly("org.metaborg:coronium:0.3.6")
  compileOnly("biz.aQute.bnd:biz.aQute.bnd.gradle:5.2.0")
  compileOnly("org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.6.5")
}

gradlePlugin {
  plugins {
    create("spoofax-compiler-language") {
      id = "org.metaborg.spoofax.compiler.gradle.language"
      implementationClass = "mb.spoofax.compiler.gradle.plugin.LanguagePlugin"
    }
    create("spoofax-compiler-adapter") {
      id = "org.metaborg.spoofax.compiler.gradle.adapter"
      implementationClass = "mb.spoofax.compiler.gradle.plugin.AdapterPlugin"
    }
    create("spoofax-compiler-cli") {
      id = "org.metaborg.spoofax.compiler.gradle.cli"
      implementationClass = "mb.spoofax.compiler.gradle.plugin.CliPlugin"
    }
    create("spoofax-compiler-eclipse") {
      id = "org.metaborg.spoofax.compiler.gradle.eclipse"
      implementationClass = "mb.spoofax.compiler.gradle.plugin.EclipsePlugin"
    }
    create("spoofax-compiler-intellij") {
      id = "org.metaborg.spoofax.compiler.gradle.intellij"
      implementationClass = "mb.spoofax.compiler.gradle.plugin.IntellijPlugin"
    }
  }
}
