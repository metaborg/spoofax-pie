import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
  id("org.metaborg.gradle.config.junit-testing")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
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
      .defaultBasePackageId("mb.mod"),

    parser = ParserCompiler.LanguageProjectInput.builder()
      .startSymbol("Start"),
    styler = StylerCompiler.LanguageProjectInput.builder(),
    completer = CompleterCompiler.LanguageProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.LanguageProjectInput.builder()
      .enableNaBL2(false)
      .enableStatix(true)
      .copyCTree(true)
      .copyClasses(false)
      .copyJavaStrategyClasses(false),
    constraintAnalyzer = ConstraintAnalyzerCompiler.LanguageProjectInput.builder()
      .multiFile(true),

    builder = LanguageProjectCompiler.Input.builder()
      .languageSpecificationDependency(GradleDependency.project(":mod.spoofaxcore"))
  ))
}

ecj {
  toolVersion = "3.20.0"
}
