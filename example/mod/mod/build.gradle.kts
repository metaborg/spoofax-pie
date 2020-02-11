import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
  id("org.metaborg.gradle.config.junit-testing")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
}

dependencies {
  api(platform("org.metaborg:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.noop")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProjectCompiler {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.LanguageProjectCompilerSettings(
    parser = ParserCompiler.LanguageProjectInput.builder()
      .startSymbol("Start"),
    styler = StylerCompiler.LanguageProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.LanguageProjectInput.builder()
      .addNaBL2Primitives(false)
      .addStatixPrimitives(true)
      .copyCTree(true)
      .copyClasses(false)
      .copyJavaStrategyClasses(false),
    constraintAnalyzer = ConstraintAnalyzerCompiler.LanguageProjectInput.builder(),
    compiler = LanguageProjectCompiler.Input.builder()
      .languageSpecificationDependency(GradleDependency.project(":mod.spoofaxcore"))
  ))
}

ecj {
  toolVersion = "3.20.0"
}
