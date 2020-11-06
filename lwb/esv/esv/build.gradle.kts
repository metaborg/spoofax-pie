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
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
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
      addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.esv.InteropRegisterer")
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

    // Use group ID "org.metaborg.bootstraphack" when building as part of devenv (not standalone).
    val spoofax2GroupId = if(gradle.parent?.rootProject?.name == "spoofax3.root") "org.metaborg" else "org.metaborg.bootstraphack"
    val spoofax2Version = System.getProperty("spoofax2Version")
    project.languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:org.metaborg.meta.lang.esv:$spoofax2Version"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.esv"
  val taskPackageId = "$packageId.task"

  val compile = TypeInfo.of(taskPackageId, "EsvCompile")
  addTaskDefs(compile)
}
