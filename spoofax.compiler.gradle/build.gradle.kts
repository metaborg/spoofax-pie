plugins {
  id("org.metaborg.gradle.config.kotlin-gradle-plugin")
  id("org.metaborg.gitonium")
  kotlin("jvm")
  id("org.gradle.kotlin.kotlin-dsl") // Same as `kotlin-dsl`, but without version, which is already set in root project.
}

metaborg {
  kotlinApiVersion = "1.2"
  kotlinLanguageVersion = "1.2"
}

dependencies {
  implementation(project(":spoofax.compiler"))
}

gradlePlugin {
  plugins {
    create("spoofax-compiler-spoofaxcore-root") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.root"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.RootPlugin"
    }
    create("spoofax-compiler-spoofaxcore-language") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.language"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.LanguagePlugin"
    }
    create("spoofax-compiler-spoofaxcore-adapter") {
      id = "org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter"
      implementationClass = "mb.spoofax.compiler.gradle.spoofaxcore.AdapterPlugin"
    }
  }
}
