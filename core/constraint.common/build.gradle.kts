plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":aterm.common"))
    api(project(":jsglr.common"))
    api(project(":stratego.common"))

    api("org.metaborg:log.api")

    api("org.metaborg.devenv:nabl2.terms")
    api("org.metaborg.devenv:org.metaborg.util")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.derive4j:derive4j-annotation")

    annotationProcessor("org.derive4j:derive4j")
}
