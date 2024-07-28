plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.spoofax.compiler.gradle.cli")
}

languageCliProject {
    adapterProject.set(project(":esv"))
}

tasks {
    // Disable currently unused distribution tasks.
    distZip.configure { enabled = false }
    distTar.configure { enabled = false }
    startScripts.configure { enabled = false }
}
