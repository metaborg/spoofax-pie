import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

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
    name("SPT")
    defaultClassPrefix("Spt")
    defaultPackageId("mb.spt")
  }
  compilerInput {
    withParser().run {
      startSymbol("TestSuite")
    }
    withStyler()
    withStrategoRuntime().run {
      addStrategyPackageIds("org.metaborg.meta.lang.spt.trans")
      addInteropRegisterersByReflection("org.metaborg.meta.lang.spt.trans.InteropRegisterer")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.module("org.metaborg.devenv:org.metaborg.meta.lang.spt:${ext["spoofax2DevenvVersion"]}"))
  }
}

val packageId = "mb.spt"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  // Extend component
  baseComponent(packageId, "BaseSptComponent")
  extendComponent(packageId, "SptComponent")

  // Wrap Check and rename base tasks
  isMultiFile(false)
  baseCheckTaskDef(spoofaxTaskPackageId, "BaseSptCheck")
  baseCheckMultiTaskDef(spoofaxTaskPackageId, "BaseSptCheckMulti")
  extendCheckTaskDef(spoofaxTaskPackageId, "SptCheckWrapper")

  // Internal task definitions
  val check = TypeInfo.of(taskPackageId, "SptCheck")
  addTaskDefs(check)
}
