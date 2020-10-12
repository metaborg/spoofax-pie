import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax3.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax3.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax3.language")
}

dependencies {
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("calc")
    defaultPackageId("mb.calc")
    defaultClassPrefix("Calc")
  }
  compilerInput {
    withParser().run {
      startSymbol("Program")
    }
    withStrategoRuntime()
  }
}

spoofax3BasedLanguageProject {
  compilerInput {
    withParser()
    withStrategoRuntime()
  }
}
