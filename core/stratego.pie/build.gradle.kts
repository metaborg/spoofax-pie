plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":stratego.common"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.javax.inject)
    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
