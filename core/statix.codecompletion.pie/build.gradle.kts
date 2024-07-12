plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":aterm.common"))
    api(project(":statix.common"))
    api(project(":spoofax.core"))
    api(project(":constraint.pie"))
    api(project(":jsglr.pie"))
    api(project(":stratego.pie"))
    api(libs.metaborg.pie.api)

    api(project(":statix.codecompletion"))

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
