plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    implementation(libs.metaborg.common)
    implementation(project(":stratego.common"))
    implementation(libs.metaborg.util)

    api(libs.metaborg.resource.api)
    api(libs.metaborg.log.api)

    implementation(libs.interpreter.core)

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
