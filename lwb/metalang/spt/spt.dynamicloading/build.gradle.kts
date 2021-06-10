plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":spt"))
  api(project(":spoofax.lwb.dynamicloading"))
}
