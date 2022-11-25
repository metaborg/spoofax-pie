plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.7"
  id("org.metaborg.gitonium") version "0.1.5"
  id("com.vanniktech.dependency.graph.generator") version "0.8.0"
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
  setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
  setProperty("termsOfServiceAgree", "yes")
}
