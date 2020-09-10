import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
}

spoofaxLanguageProject {
  settings.set(LanguageProjectSettings().apply {
    shared
      .name("Stratego")
      .defaultClassPrefix("Stratego")
      .defaultPackageId("mb.str")

    builder.run {
      parser = ParserLanguageCompiler.Input.builder()
        .startSymbol("Module")
      styler = StylerLanguageCompiler.Input.builder()
      strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder()
        .addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
    }

    spoofax2Builder.run {
      strategoRuntime = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder()
        .copyCtree(false)
        .copyClasses(true)
      languageProject
        .languageSpecificationDependency(GradleDependency.module("org.metaborg:org.metaborg.meta.lang.stratego:2.5.11"))
    }
  })
}
