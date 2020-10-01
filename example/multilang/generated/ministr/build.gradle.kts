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
  api(project(":module"))
}

languageProject {
  shared {
    name("MiniStr")
    fileExtensions(listOf("mstr"))
    defaultPackageId("mb.ministr")
  }
  compilerInput {
    withParser().run {
      startSymbol("MSTRStart")
    }
    withStyler()
    withMultilangAnalyzer().run {
      rootModules(listOf("mini-str/mini-str-typing"))
    }
    withStrategoRuntime()
  }
  statixDependencies.set(listOf(project(":module")))
}

spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    withMultilangAnalyzer()
    project
      .languageSpecificationDependency(GradleDependency.project(":ministr.spoofaxcore"))
  }
}
