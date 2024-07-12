plugins {
    `maven-publish`
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    testImplementation(platform(libs.metaborg.platform))

    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}
