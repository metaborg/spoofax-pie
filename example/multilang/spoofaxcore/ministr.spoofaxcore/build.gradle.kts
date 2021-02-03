plugins {
  id("org.metaborg.devenv.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

val spoofax2DevenvVersion: String by ext
spoofaxLanguageSpecification {
  addSourceDependenciesFromMetaborgYaml.set(false)
  addCompileDependenciesFromMetaborgYaml.set(false)
}
dependencies {
  compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.template:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.esv:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:statix.lang:$spoofax2DevenvVersion")
  compileLanguage("org.metaborg.devenv:sdf3.ext.statix:$spoofax2DevenvVersion")

  sourceLanguage("org.metaborg.devenv:meta.lib.spoofax:$spoofax2DevenvVersion")
  sourceLanguage("org.metaborg.devenv:statix.runtime:$spoofax2DevenvVersion")
  sourceLanguage(project(":signature-interface.spoofaxcore"))
  sourceLanguage(project(":module-interface.spoofaxcore"))
}

ecj {
  toolVersion = "3.21.0"
}
tasks.withType<JavaCompile> { // ECJ does not support headerOutputDirectory (-h argument).
  options.headerOutputDirectory.convention(provider { null })
}

afterEvaluate {
  val importSignature = tasks.register<Sync>("importSignature") {
    from("../signature-interface.spoofaxcore/trans/abstract-sig")
    into("trans/abstract-sig")
  }
  val importModule = tasks.register<Sync>("importModule") {
    from("../module-interface.spoofaxcore/trans/modules")
    into("trans/modules")
  }
  tasks.getByName("spoofaxGenerateSources") {
    dependsOn(importSignature)
    dependsOn(importModule)
  }
}
