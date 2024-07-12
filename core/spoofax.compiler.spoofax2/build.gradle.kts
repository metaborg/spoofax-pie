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
    api(project(":spoofax.compiler"))
    api(libs.metaborg.resource.api)
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.immutables.value)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
