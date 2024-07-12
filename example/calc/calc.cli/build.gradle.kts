plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.cli")
}

languageCliProject {
    adapterProject.set(project(":calc"))
}

tasks {
    // Disable currently unused distribution tasks.
    distZip.configure { enabled = false }
    distTar.configure { enabled = false }
    startScripts.configure { enabled = false }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
