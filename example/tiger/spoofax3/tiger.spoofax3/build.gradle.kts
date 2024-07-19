plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
