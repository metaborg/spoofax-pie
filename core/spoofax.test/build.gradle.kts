plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api(project(":spoofax.core"))
    api(project(":spoofax.compiler.interfaces"))
    api(libs.metaborg.log.backend.slf4j)
    api(libs.slf4j.simple)
    api(libs.jimfs)
    api(libs.metaborg.pie.runtime)

    api(libs.junit.api)

    compileOnly(libs.checkerframework.android)
}
