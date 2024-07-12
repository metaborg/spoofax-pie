plugins {
    id("org.metaborg.gradle.config.java-library")
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
