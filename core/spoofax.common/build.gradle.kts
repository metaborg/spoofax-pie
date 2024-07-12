plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    compileOnly(libs.checkerframework.android)
}
