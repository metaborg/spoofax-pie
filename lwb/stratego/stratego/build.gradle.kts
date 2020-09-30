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

languageProject {
  shared {
    name("Stratego")
    defaultClassPrefix("Stratego")
    defaultPackageId("mb.str")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withStrategoRuntime().run {
      addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }
    project
      .languageSpecificationDependency(GradleDependency.module("org.metaborg.bootstraphack:org.metaborg.meta.lang.stratego:2.5.11"))
  }
}
