plugins {
  id("org.metaborg.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

ecj {
  toolVersion = "3.21.0"
}
tasks.withType<JavaCompile> { // ECJ does not support headerOutputDirectory (-h argument).
  options.headerOutputDirectory.convention(provider { null })
}

dependencies {
  sourceLanguage(project(":signature-interface.spoofaxcore"))
  sourceLanguage(project(":module-interface.spoofaxcore"))
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
