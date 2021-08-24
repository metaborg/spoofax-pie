import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  implementation("$group:spoofax2.common:$version")

  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("Statix")
    addFileExtensions("stx", "stxtest")
    defaultClassPrefix("Statix")
    defaultPackageId("mb.statix")
  }
  compilerInput {
    withParser().run {
      startSymbol("Start")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(true)
      enableStatix(false)
      multiFile(true)
    }
    withStrategoRuntime().run {
      addStrategyPackageIds("statix.lang.strategies")
      addStrategyPackageIds("statix.lang.trans")
      addInteropRegisterersByReflection("statix.lang.strategies.InteropRegisterer")
      addInteropRegisterersByReflection("statix.lang.trans.InteropRegisterer")
      addSpoofax2Primitives(true)
      addStatixPrimitives(true) // Requires the STX_compare_patterns primitive.
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }
    project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:statix.lang:${ext["spoofax2DevenvVersion"]}"))
  }
}

val packageId = "mb.statix"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"
languageAdapterProject {
  compilerInput {
    withParser().run {
      // Wrap Parse task
      extendParseTaskDef(spoofaxTaskPackageId, "StatixParseWrapper")
    }
    withStyler()
    withConstraintAnalyzer().run {
      // Wrap AnalyzeMulti and rename base task
      baseAnalyzeMultiTaskDef(spoofaxTaskPackageId, "BaseStatixAnalyzeMulti")
      extendAnalyzeMultiTaskDef(spoofaxTaskPackageId, "StatixAnalyzeMultiWrapper")
    }
    withStrategoRuntime()
    withReferenceResolution().run {
      resolveStrategy("nabl2--editor-resolve")
    }
    withHover().run {
      hoverStrategy("nabl2--editor-hover")
    }
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  // Extend component
  baseComponent(packageId, "BaseStatixComponent")
  extendComponent(packageId, "StatixComponent")

  // Wrap CheckMulti and rename base tasks
  isMultiFile(true)
  baseCheckTaskDef(spoofaxTaskPackageId, "BaseStatixCheck")
  baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseStatixCheckMulti")
  extendCheckMultiTaskDef(spoofaxTaskPackageId, "StatixCheckMultiWrapper")

  addTaskDefs(taskPackageId, "StatixPrettyPrint")

  addTaskDefs(taskPackageId, "StatixCheck")
  addTaskDefs(taskPackageId, "StatixCompile")
}
