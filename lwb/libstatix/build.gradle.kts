import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
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
      languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:statix.runtime:${ext["spoofax2DevenvVersion"]}"))
    }
  }
}

languageAdapterProject {
  compilerInput {
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {

}
