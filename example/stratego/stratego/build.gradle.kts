import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
}

spoofaxLanguageProject {
  settings.set(LanguageProjectSettings(
    shared = Shared.builder()
      .name("Stratego")
      .defaultClassPrefix("Stratego")
      .defaultPackageId("mb.str"),

    parser = ParserLanguageCompiler.Input.builder()
      .startSymbol("Module"),
    styler = StylerLanguageCompiler.Input.builder(),
    completer = CompleterLanguageCompiler.Input.builder(),
    strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder()
      .addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
      .enableNaBL2(false)
      .enableStatix(false)
      .copyCTree(false)
      .copyClasses(true),

    builder = run {
      val builder = LanguageProjectCompiler.Input.builder()
      builder.languageSpecificationDependency(GradleDependency.module("org.metaborg:org.metaborg.meta.lang.stratego:2.5.11"))
      builder
    }
  ))
}
