import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageProject {
  shared {
    name("LibSpoofax2")
    defaultClassPrefix("LibSpoofax2")
    defaultPackageId("mb.libspoofax2")
  }
  compilerInput {
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    project.run {
      addAdditionalCopyResources(
        "trans/**/*.str",
        "trans/**/*.str2",
        "editor/**/*.esv"
      )
      languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:meta.lib.spoofax:${ext["spoofax2DevenvVersion"]}"))
    }
  }
}

languageAdapterProject {
  compilerInput {
    project.configureCompilerInput()
    withExports().run {
      addDirectoryExport("Stratego", "trans")
      addDirectoryExport("Esv", "editor")
    }
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  compositionGroup("mb.spoofax.lwb")
}
