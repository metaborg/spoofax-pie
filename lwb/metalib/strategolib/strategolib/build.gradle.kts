import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageProject {
  shared {
    name("StrategoLib")
    defaultClassPrefix("StrategoLib")
    defaultPackageId("mb.strategolib")
  }
  compilerInput {
    withStrategoRuntime().run {
      addStrategyPackageIds("strategolib.trans")
      addInteropRegisterersByReflection("strategolib.trans.InteropRegisterer")
    }
    withExports().run {
      addExports("Stratego", "trans")
      addExports("Str2Lib", "src-gen/java/strategolib/trans/strategolib.str2lib")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }
    project.run {
      addAdditionalCopyResources(
        "trans/**/*.str",
        "trans/**/*.str2",
        "src-gen/java/strategolib/trans/strategolib.str2lib"
      )
      languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:strategolib:${ext["spoofax2DevenvVersion"]}"))
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
