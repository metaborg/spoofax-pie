import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageAdapterProject {
  languageProject.set(project(":minisdf"))
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    withMultilangAnalyzer().run {
      rootModule("mini-sdf/mini-sdf-typing")
      preAnalysisStrategy("pre-analyze")
      postAnalysisStrategy("post-analyze")
      contextId("mini-sdf-str")
      fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
      projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK")
    }
  }
}
