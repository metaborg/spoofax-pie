plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)

    api(libs.metaborg.log.dagger)
    api(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.checkerframework.android)

    testCompileOnly(libs.checkerframework.android)
    testImplementation(libs.metaborg.log.backend.slf4j)
    testImplementation(libs.slf4j.simple)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
