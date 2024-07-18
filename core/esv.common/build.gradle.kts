plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)
    api(libs.spoofax.terms)
    implementation(libs.jsglr) // TODO: avoid dependency to jsglr, only need it for imploder attachment.

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
