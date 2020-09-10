import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
}

dependencies {
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

spoofaxLanguageProject {
  settings.set(LanguageProjectSettings(
    shared = Shared.builder()
      .name("Mod")
      .defaultPackageId("mb.mod"),

    parser = ParserLanguageCompiler.Input.builder()
      .startSymbol("Start"),
    styler = StylerLanguageCompiler.Input.builder(),
    completer = CompleterLanguageCompiler.Input.builder(),
    constraintAnalyzer = ConstraintAnalyzerLanguageCompiler.Input.builder()
      .enableNaBL2(false)
      .enableStatix(true)
      .multiFile(true),
    strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder(),

    spoofax2ConstraintAnalyzer = Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder()
      .copyStatix(true),
    spoofax2StrategoRuntime = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder()
      .copyCtree(true)
      .copyClasses(false),
    spoofax2Builder = Spoofax2LanguageProjectCompiler.Input.builder()
      .languageSpecificationDependency(GradleDependency.project(":mod.spoofaxcore"))
  ))
}
