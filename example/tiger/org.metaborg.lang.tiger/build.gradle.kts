plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.12"
  id("org.metaborg.gitonium") version "0.1.2"
  id("org.metaborg.spoofax.gradle.langspec") version "0.2.1"
  id("de.set.ecj") version "1.4.1" // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

spoofax {
  createPublication = true
}
