plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(libs.metaborg.resource.api)

    compileOnly(libs.checkerframework.android)
}
