plugins {
  id("org.metaborg.gradle.config.root-project") version "0.5.6"
  id("org.metaborg.gitonium") version "0.1.5"
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
  try {
    // New Developcity plugin
    setProperty("termsOfUseUrl", "https://gradle.com/help/legal-terms-of-use")
    setProperty("termsOfUseAgree", "yes")
  } catch (ex: groovy.lang.MissingPropertyException) {
    // Deprecated Gradle Enterprise plugin
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
  }
}
