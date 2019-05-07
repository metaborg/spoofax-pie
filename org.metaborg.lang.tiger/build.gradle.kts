plugins {
  id("org.metaborg.spoofax.gradle.langspec") version "develop-SNAPSHOT"
  id("de.set.ecj") version "1.4.1" // Use ECJ to speed up compilation of Stratego's generated Java files.
}

spoofax {
  createPublication = false
}
