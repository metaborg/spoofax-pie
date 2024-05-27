plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":aterm.common"))
    api(project(":statix.common"))
    api(project(":spoofax.core"))
    api(project(":constraint.pie"))
    api(project(":jsglr.pie"))
    api(project(":stratego.pie"))
    api("org.metaborg:pie.api")

    api(project(":statix.codecompletion"))

    compileOnly("org.checkerframework:checker-qual-android")
}
