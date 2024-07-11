plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(project(":aterm.common"))
    api(project(":statix.common"))
    api(project(":spoofax.core"))
    api(project(":constraint.pie"))
    api(project(":jsglr.pie"))
    api(project(":stratego.pie"))
    api(libs.metaborg.pie.api)

    api(project(":statix.codecompletion"))

    compileOnly(libs.checkerframework.android)
}
