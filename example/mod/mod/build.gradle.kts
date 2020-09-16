import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

dependencies {
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  settings.set(LanguageProjectSettings().apply {
    shared
      .name("Mod")
      .defaultPackageId("mb.mod")

    builder.run {
      withParser { it.startSymbol("Start") }
      withStyler()
      withConstraintAnalyzer {
        it.enableNaBL2(false)
          .enableStatix(true)
          .multiFile(true)
      }
      withStrategoRuntime()
    }
  })
}

spoofax2BasedLanguageProject {
  settings.set(Spoofax2LanguageProjectSettings().apply {
    builder.run {
      withParser()
      withStyler()
      withConstraintAnalyzer {
        it.copyStatix(true)
      }
      withStrategoRuntime {
        it.copyCtree(true)
          .copyClasses(false)
      }
      languageProject
        .languageSpecificationDependency(GradleDependency.project(":mod.spoofaxcore"))
    }
  })
}
