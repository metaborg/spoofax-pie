plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.intellij")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the IntelliJ plugin (via spoofax.compiler.gradle.intellij) defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

languageIntellijProject {
    adapterProject.set(project(":tiger.spoofax3"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
