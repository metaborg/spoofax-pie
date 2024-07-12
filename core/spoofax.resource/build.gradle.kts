plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(libs.metaborg.resource.api)

    compileOnly(libs.checkerframework.android)
}
