import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

languageProject {
  shared {
    name("LibSpoofax2")
    defaultClassPrefix("LibSpoofax2")
    defaultPackageId("mb.libspoofax2")
  }
  compilerInput {
    withExports().run {
      addExports("Stratego", "trans")
      addExports("ESV", "editor")
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    project.run {
      addAdditionalCopyResources("trans/**/*.str", "editor/**/*.esv")
      languageSpecificationDependency(GradleDependency.module("org.metaborg.bootstraphack:meta.lib.spoofax:2.5.11"))
    }
  }
}
