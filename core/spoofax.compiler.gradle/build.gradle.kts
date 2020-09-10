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
  api(project(":spoofax.compiler.spoofax2"))
  api(project(":spoofax.compiler.spoofax2.dagger"))
  api("com.google.dagger:dagger")

  implementation("org.metaborg:pie.runtime")

  kapt("com.google.dagger:dagger-compiler")
  compileOnly("org.immutables:value-annotations") // Dagger accesses these annotations, which have class retention.

  // Dependencies to be able to configure the extensions provided by these Gradle plugins.
  compileOnly("org.metaborg:coronium:0.3.0")
  compileOnly("biz.aQute.bnd:biz.aQute.bnd.gradle:5.0.1")
  compileOnly("gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.4.21")
}

gradlePlugin {
  plugins {
    create("spoofax-compiler-spoofaxcore-language") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.language"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.LanguagePlugin"
    }
    create("spoofax-compiler-spoofaxcore-adapter") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.AdapterPlugin"
    }
    create("spoofax-compiler-spoofaxcore-cli") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.CliPlugin"
    }
    create("spoofax-compiler-spoofaxcore-eclipse-externaldeps") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.EclipseExternaldepsPlugin"
    }
    create("spoofax-compiler-spoofaxcore-eclipse") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.EclipsePlugin"
    }
    create("spoofax-compiler-spoofaxcore-intellij") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.intellij"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.IntellijPlugin"
    }
  }
}
