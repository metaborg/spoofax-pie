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
