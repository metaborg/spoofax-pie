plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
    adapterProject.set(project(":libstatix"))
}

mavenize {
    majorVersion.set("2022-06")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
