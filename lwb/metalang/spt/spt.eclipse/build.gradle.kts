plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

dependencies {
    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)
}

languageEclipseProject {
    adapterProject.set(project(":spt"))
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
