plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    implementation(libs.metaborg.common)
    implementation(project(":stratego.common"))
    implementation(libs.metaborg.util)

    api(libs.metaborg.resource.api)
    api(libs.metaborg.log.api)

    implementation(libs.interpreter.core)

    compileOnly(libs.checkerframework.android)
}
