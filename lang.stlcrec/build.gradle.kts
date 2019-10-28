plugins {
  id("org.metaborg.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
}

spoofax {
  createPublication = false
  runTests = false // Disable tests: Statix tests are broken.
}
