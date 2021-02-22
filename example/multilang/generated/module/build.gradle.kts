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
  api(project(":signature"))
}

languageProject {
  shared {
    name("Module")
    defaultPackageId("mb.module")
  }
  compilerInput {
    withMultilangAnalyzer().run {
      rootModules(listOf("modules/modules"))
    }
  }
  statixDependencies.set(listOf(project(":signature")))
}

spoofax2BasedLanguageProject {
  compilerInput {
    withMultilangAnalyzer()
    project
      .languageSpecificationDependency(GradleDependency.project(":module-interface.spoofaxcore"))
  }
}

languageAdapterProject {

}
