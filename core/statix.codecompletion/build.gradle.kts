plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.common)
    api(libs.spoofax3.statix.common)
    api(libs.spoofax3.tego.runtime)
    api(libs.metaborg.log.api)

    api(libs.statix.solver)
    api(libs.statix.generator)

    implementation(project(":stratego.common"))
    implementation(project(":jsglr.common"))

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.immutables.value)
    testAnnotationProcessor(libs.immutables.value)

    testCompileOnly(libs.checkerframework.android)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.metaborg.log.backend.slf4j)
    testImplementation(libs.slf4j.simple)
    testCompileOnly(libs.immutables.value)
    testImplementation(libs.opencsv)

    // Immutables
    testCompileOnly(libs.immutables.value)
    testAnnotationProcessor(libs.immutables.value)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
