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

}

languageProject {
  shared {
    name("Signature")
    defaultPackageId("mb.signature")
  }
  compilerInput {
    /* withParser().run {
      startSymbol("MSDFStart")
    }
    withStyler() */
    /* withConstraintAnalyzer().run {
      enableNaBL2(false)
      enableStatix(true)
      multiFile(true)
    } */
    withStrategoRuntime()
    withMultilangAnalyzer().run {
      rootModules(listOf("cons-type-interface/conflicts/sorts", "cons-type-interface/conflicts/constructors"))
    }
  }
  statixDependencies.set(listOf())
}

spoofax2BasedLanguageProject {
  compilerInput {
    /* withParser()
    withStyler() */
    /* withConstraintAnalyzer().run {
      copyStatix(true)
    } */
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project
      .languageSpecificationDependency(GradleDependency.project(":signature-interface.spoofaxcore"))
    withMultilangAnalyzer()
  }
}
