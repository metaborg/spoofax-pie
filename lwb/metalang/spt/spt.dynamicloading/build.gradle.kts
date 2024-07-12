plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(project(":spt"))
    api(project(":spoofax.lwb.dynamicloading"))

    compileOnly(libs.checkerframework.android)
}
