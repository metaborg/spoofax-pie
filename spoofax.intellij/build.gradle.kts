import org.jetbrains.intellij.IntelliJPluginExtension

plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.jetbrains.intellij") version "0.4.8"
}

apply {
    plugin("org.jetbrains.intellij")
}

dependencies {
    implementation(platform(project(":depconstraints")))

    implementation(project(":spoofax.intellij"))
    implementation(project(":tiger"))
    implementation(project(":tiger.spoofax"))
}

configure<IntelliJPluginExtension> {
    version = "2019.1.1"
}
