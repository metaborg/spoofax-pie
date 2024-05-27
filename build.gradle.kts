plugins {
  id("org.metaborg.gradle.config.root-project") version "0.5.6"
  id("org.metaborg.gitonium") version "1.2.0"
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
  try {
    // New Develocity plugin
    setProperty("termsOfUseUrl", "https://gradle.com/help/legal-terms-of-use")
    setProperty("termsOfUseAgree", "yes")
  } catch (ex: groovy.lang.MissingPropertyException) {
    // Deprecated Gradle Enterprise plugin
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
  }
}
