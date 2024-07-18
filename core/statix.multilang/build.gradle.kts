plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.common)
    api(project(":jsglr.common"))
    api(project(":stratego.common"))
    api(project(":spoofax.core"))

    api(libs.statix.solver)
    api(libs.metaborg.pie.api)
    api(libs.snakeyaml)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.immutables.value)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
