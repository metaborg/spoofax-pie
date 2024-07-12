plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)

    api(libs.nabl2.solver)

    compileOnly(libs.checkerframework.android)
}
