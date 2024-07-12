plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

mavenize {
    majorVersion.set("2022-06")
}

languageEclipseProject {
    adapterProject.set(project(":minisdf"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
