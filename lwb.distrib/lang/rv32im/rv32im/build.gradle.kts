plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

repositories {
  maven("https://raw.github.com/chocopy/venus/maven-repository/")
}

dependencies {
  implementation("edu.berkeley.eecs.venus164:venus164:0.2.5")
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
}
