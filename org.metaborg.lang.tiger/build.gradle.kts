plugins {
  id("org.metaborg.spoofax.gradle.langspec") version "0.1.6"
  id("de.set.ecj") version "1.4.1" // Use ECJ to speed up compilation of Stratego's generated Java files.
}

spoofax {
  createPublication = false
}
