plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)
    api(libs.spoofax.terms)
    compileOnly(libs.derive4j.annotation)
    compileOnly(libs.checkerframework.android)
    annotationProcessor(libs.derive4j)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
