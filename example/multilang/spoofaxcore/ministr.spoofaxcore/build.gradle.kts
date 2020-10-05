plugins {
  id("org.metaborg.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

ecj {
  toolVersion = "3.20.0"
}

dependencies {
  sourceLanguage(project(":signature-interface.spoofaxcore"))
}
