plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)
    api(libs.spoofax.terms)
    implementation(libs.jsglr) // TODO: avoid dependency to jsglr, only need it for imploder attachment.

    compileOnly(libs.checkerframework.android)
}
