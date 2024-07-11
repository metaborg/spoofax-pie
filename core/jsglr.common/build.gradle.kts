plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)

    api(libs.metaborg.resource.api)
    api(libs.jsglr.shared)
    api(libs.jsglr)
    api(libs.spoofax.terms)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}
