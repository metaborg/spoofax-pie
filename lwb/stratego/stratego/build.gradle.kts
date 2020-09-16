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
  settings.set(LanguageProjectSettings().apply {
    shared
      .name("Stratego")
      .defaultClassPrefix("Stratego")
      .defaultPackageId("mb.str")

    builder.run {
      withParser {
        it.startSymbol("Module")
      }
      withStyler()
      withStrategoRuntime {
        it.addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
      }
    }
  })
}

spoofax2BasedLanguageProject {
  settings.set(Spoofax2LanguageProjectSettings().apply {
    builder.run {
      withParser()
      withStyler()
      withStrategoRuntime {
        it.copyCtree(false)
          .copyClasses(true)
      }
      languageProject
        .languageSpecificationDependency(GradleDependency.module("org.metaborg:org.metaborg.meta.lang.stratego:2.5.11"))
    }
  })
}
