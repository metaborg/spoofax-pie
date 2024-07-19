plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin (via Spoofax.Compiler applying Coronium) defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
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
