import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.12"
  id("org.metaborg.gitonium") version "0.1.2"
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.root")

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.coronium.bundle") version "0.1.8" apply false
  id("org.metaborg.coronium.embedding") version "0.1.8" apply false
  id("net.ltgt.apt") version "0.21" apply false
  id("net.ltgt.apt-idea") version "0.21" apply false
  id("biz.aQute.bnd.builder") version "4.3.1" apply false
  id("com.palantir.graal") version "0.6.0" apply false
  id("org.jetbrains.intellij") version "0.4.15" apply false
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

allprojects {
  repositories {
    // Required by NaBL2/Statix solver.
    maven("http://nexus.usethesource.io/content/repositories/public/")
  }
}

gitonium {
  // Disable snapshot dependency checks for releases, until we depend on a stable version of MetaBorg artifacts.
  checkSnapshotDependenciesInRelease = false
}

spoofaxCompiler {
  sharedBuilder.set(Shared.builder()
    .name("Tiger")
    .defaultBasePackageId("mb.tiger")
  )
  parserBuilder.set(ParserCompiler.LanguageProjectInput.builder()
    .startSymbol("Start")
  )
  stylerBuilder.set(StylerCompiler.LanguageProjectInput.builder())
  strategoRuntimeBuilder.set(StrategoRuntimeCompiler.LanguageProjectInput.builder()
    .addInteropRegisterersByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer", "org.metaborg.lang.tiger.strategies.InteropRegisterer")
    .addNaBL2Primitives(true)
    .addStatixPrimitives(false)
    .copyJavaStrategyClasses(true)
  )
  constraintAnalyzerBuilder.set(ConstraintAnalyzerCompiler.LanguageProjectInput.builder())
  languageProjectBuilder.set(LanguageProjectCompiler.Input.builder()
    .languageSpecificationDependency(GradleDependency.module("$group:org.metaborg.lang.tiger:$version"))
  )
  adapterProjectBuilder.set(AdapterProjectCompiler.Input.builder()
    .languageProjectDependency(GradleDependency.project(":tiger"))
  )
}
