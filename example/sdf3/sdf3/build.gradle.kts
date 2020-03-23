import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.GradleDependency

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
  id("org.metaborg.gradle.config.junit-testing")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
}

dependencies {
  api(platform("org.metaborg:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProjectCompiler {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.LanguageProjectCompilerSettings(
    parser = ParserCompiler.LanguageProjectInput.builder()
      .startSymbol("Module"),
    styler = StylerCompiler.LanguageProjectInput.builder(),
    completer = CompleterCompiler.LanguageProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.LanguageProjectInput.builder()
      .enableNaBL2(false)
      .enableStatix(true)
      .copyCTree(true)
      .copyClasses(false)
      .copyJavaStrategyClasses(false)
      .classKind(mb.spoofax.compiler.util.ClassKind.Extended)
      .manualFactory("mb.sdf3", "Sdf3ManualStrategoRuntimeBuilderFactory"),
    constraintAnalyzer = ConstraintAnalyzerCompiler.LanguageProjectInput.builder()
      .strategoStrategy("statix-editor-analyze")
      .multiFile(true),
    compiler = LanguageProjectCompiler.Input.builder()
      // HACK: use org.metaborggggg groupId for SDF3, as that is used to prevent bootstrapping issues.
      .languageSpecificationDependency(GradleDependency.module("org.metaborggggg:org.metaborg.meta.lang.template:2.6.0-SNAPSHOT"))
  ))
}

ecj {
  toolVersion = "3.20.0"
}
