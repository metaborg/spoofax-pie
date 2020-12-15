import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  api(project(":module"))
}

languageProject {
  shared {
    name("MiniSdf")
    fileExtensions(listOf("msdf"))
    defaultPackageId("mb.minisdf")
  }
  compilerInput {
    withParser().run {
      startSymbol("Start")
    }
    withStyler()
    withMultilangAnalyzer().run {
      rootModules(listOf("mini-sdf"))
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
      .languageSpecificationDependency(GradleDependency.project(":minisdf.spoofaxcore"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    withMultilangAnalyzer().run {
      preAnalysisStrategy("pre-analyze")
      postAnalysisStrategy("post-analyze")
      contextId("mini-sdf-str")
      fileConstraint("mini-sdf!fileOk")
      projectConstraint("mini-sdf!projectOk")
    }
  }
}
