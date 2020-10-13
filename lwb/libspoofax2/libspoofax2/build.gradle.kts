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
      addExports("Esv", "editor")
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    project.run {
      addAdditionalCopyResources("trans/**/*.str", "editor/**/*.esv")

      // Use group ID "org.metaborg.bootstraphack" when building as part of devenv (not standalone).
      val spoofax2GroupId = if(gradle.parent?.rootProject?.name == "spoofax3.root") "org.metaborg" else "org.metaborg.bootstraphack"
      val spoofax2Version = System.getProperty("spoofax2Version")
      languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:meta.lib.spoofax:$spoofax2Version"))
    }
  }
}
