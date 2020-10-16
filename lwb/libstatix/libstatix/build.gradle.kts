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
    name("LibStatix")
    defaultClassPrefix("LibStatix")
    defaultPackageId("mb.libstatix")
  }
  compilerInput {
    withExports().run {
      addExports("Stratego", "trans")
      addExports("Stratego", "src-gen")
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    project.run {
      addAdditionalCopyResources("trans/**/*.str")
      addAdditionalCopyResources("src-gen/**/*.str")

      val spoofax2GroupId = "org.metaborg"
      val spoofax2Version = System.getProperty("spoofax2Version")
      project.languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:statix.runtime:$spoofax2Version"))
    }
  }
}
