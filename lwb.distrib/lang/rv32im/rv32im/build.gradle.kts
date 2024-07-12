plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

dependencies {
    implementation("edu.berkeley.eecs.venus164:venus164:0.2.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
