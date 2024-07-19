plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    implementation(project(":aterm.common"))
    api(project(":jsglr.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
