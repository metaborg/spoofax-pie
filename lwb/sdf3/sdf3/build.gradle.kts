import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

dependencies {
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("SDF3")
    defaultClassPrefix("Sdf3")
    defaultPackageId("mb.sdf3")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withConstraintAnalyzer().run {
      strategoStrategy("statix-editor-analyze")
      enableNaBL2(false)
      enableStatix(true)
      multiFile(true)
    }
    withStrategoRuntime().run {
      addInteropRegisterersByReflection("org.metaborg.meta.lang.template.strategies.InteropRegisterer")
      classKind(mb.spoofax.compiler.util.ClassKind.Extended)
      manualFactory("mb.sdf3", "Sdf3ManualStrategoRuntimeBuilderFactory")
    }
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer().run {
      copyStatix(true)
    }
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(true)
    }
    project.run {
      addAdditionalCopyResources("target/metaborg/EditorService-pretty.pp.af")
      // HACK: use org.metaborggggg groupId for SDF3, as that is used to prevent bootstrapping issues.
      languageSpecificationDependency(GradleDependency.module("org.metaborggggg:org.metaborg.meta.lang.template:2.5.11"))
    }
  }
}
