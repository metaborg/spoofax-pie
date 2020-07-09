import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.spoofaxcore.*
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
      .defaultBasePackageId("mb.str"),

    parser = ParserCompiler.LanguageProjectInput.builder()
      .startSymbol("Module"),
    styler = StylerCompiler.LanguageProjectInput.builder(),
    completer = CompleterCompiler.LanguageProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.LanguageProjectInput.builder()
      .addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
      .enableNaBL2(false)
      .enableStatix(false)
      .copyCTree(false)
      .copyClasses(true),

    builder = run {
      val builder = LanguageProjectCompiler.Input.builder()
      builder.languageSpecificationDependency(GradleDependency.module("org.metaborg:org.metaborg.meta.lang.stratego:2.5.10"))
      builder
    }
  ))
}
