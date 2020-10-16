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
    name("Statix")
    defaultClassPrefix("Statix")
    defaultPackageId("mb.statix")
  }
  compilerInput {
    withParser().run {
      startSymbol("Start")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(true)
      enableStatix(false)
      multiFile(true)
    }
    withStrategoRuntime().run {
      addInteropRegisterersByReflection("statix.lang.strategies.InteropRegisterer")
      addInteropRegisterersByReflection("statix.lang.trans.InteropRegisterer")
      addSpoofax2Primitives(true)
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }

    val spoofax2GroupId = "org.metaborg"
    val spoofax2Version = System.getProperty("spoofax2Version")
    project.languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:statix.lang:$spoofax2Version"))
  }
}
