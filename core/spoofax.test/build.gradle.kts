plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":spoofax.core"))
    api(project(":spoofax.compiler.interfaces"))
    api(libs.metaborg.log.backend.slf4j)
    api(libs.slf4j.simple)
    api(libs.jimfs)
    api(libs.metaborg.pie.runtime)

    api(libs.junit.api)

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
