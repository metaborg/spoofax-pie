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
      .name("SDF3")
      .defaultClassPrefix("Sdf3")
      .defaultPackageId("mb.sdf3"),

    parser = ParserLanguageCompiler.Input.builder()
      .startSymbol("Module"),
    styler = StylerLanguageCompiler.Input.builder(),
    completer = CompleterLanguageCompiler.Input.builder(),
    constraintAnalyzer = ConstraintAnalyzerLanguageCompiler.Input.builder()
      .strategoStrategy("statix-editor-analyze")
      .enableNaBL2(false)
      .enableStatix(true)
      .multiFile(true),
    strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder()
      .addInteropRegisterersByReflection("org.metaborg.meta.lang.template.strategies.InteropRegisterer")
      .classKind(mb.spoofax.compiler.util.ClassKind.Extended)
      .manualFactory("mb.sdf3", "Sdf3ManualStrategoRuntimeBuilderFactory"),

    spoofax2ConstraintAnalyzer = Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder()
      .copyStatix(true),
    spoofax2StrategoRuntime = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder()
      .copyCtree(true)
      .copyClasses(true),
    spoofax2Builder = run {
      val builder = Spoofax2LanguageProjectCompiler.Input.builder()
      builder.addAdditionalCopyResources("target/metaborg/EditorService-pretty.pp.af")
      if(gradle.parent != null && gradle.parent!!.rootProject.name == "devenv") {
        // HACK: use org.metaborggggg groupId for SDF3, as that is used to prevent bootstrapping issues.
        builder.languageSpecificationDependency(GradleDependency.module("org.metaborggggg:org.metaborg.meta.lang.template:2.5.10"))
      } else {
        // HACK: when building standalone (outside of devenv composite build), use a normal SDF3 dependency.
        builder.languageSpecificationDependency(GradleDependency.module("org.metaborg:org.metaborg.meta.lang.template:2.5.10"))
      }
      builder
    }
  ))
}

tasks.test {
  // HACK: skip if not in devenv composite build, as that is not using the latest version of SDF3.
  if(gradle.parent == null || gradle.parent!!.rootProject.name != "devenv") {
    onlyIf { false }
  }
}
