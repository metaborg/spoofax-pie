plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":jsglr1.common"))
    api(project(":stratego.common"))
    api(project(":constraint.common"))
    api(project(":tego.runtime"))

    compileOnly(libs.checkerframework.android)
}
