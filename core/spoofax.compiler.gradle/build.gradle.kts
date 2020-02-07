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

  // Dependencies to be able to configure the extensions provided by these Gradle plugins.
  compileOnly("org.metaborg:coronium:0.1.8")
  compileOnly("biz.aQute.bnd:biz.aQute.bnd.gradle:4.3.1")
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
  }
}
