plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    implementation("org.metaborg:common")
    implementation(project(":stratego.common"))
    implementation("org.metaborg.devenv:org.metaborg.util")

    api("org.metaborg:resource")
    api("org.metaborg:log.api")

    implementation("org.metaborg.devenv:org.spoofax.interpreter.core")

    compileOnly("org.checkerframework:checker-qual-android")
}
