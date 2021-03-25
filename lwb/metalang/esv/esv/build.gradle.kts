import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

languageProject {
  shared {
    name("ESV")
    defaultClassPrefix("Esv")
    defaultPackageId("mb.esv")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withStrategoRuntime().run {
      addStrategyPackageIds("org.metaborg.meta.lang.esv.trans")
      addInteropRegisterersByReflection("org.metaborg.meta.lang.esv.trans.InteropRegisterer")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }
    project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.esv:${ext["spoofax2DevenvVersion"]}"))
  }
}

val packageId = "mb.esv"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"

languageAdapterProject {
  compilerInput {
    withParser().run {
      // Wrap Parse task
      extendParseTaskDef(spoofaxTaskPackageId, "EsvParseWrapper")
    }
    withStyler()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  // Extend component
  baseComponent(packageId, "BaseEsvComponent")
  extendComponent(packageId, "EsvComponent")

  // Wrap CheckMulti and rename base tasks
  isMultiFile(true)
  baseCheckTaskDef(spoofaxTaskPackageId, "BaseEsvCheck")
  baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseEsvCheckMulti")
  extendCheckMultiTaskDef(spoofaxTaskPackageId, "EsvCheckMultiWrapper")

  // Internal task definitions
  val check = TypeInfo.of(taskPackageId, "EsvCheck")
  val compile = TypeInfo.of(taskPackageId, "EsvCompile")
  addTaskDefs(check, compile)
}
