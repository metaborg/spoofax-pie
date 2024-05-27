plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")

    api("org.metaborg:resource")
    api("org.metaborg.devenv:jsglr.shared")
    api("org.metaborg.devenv:org.spoofax.jsglr")
    api("org.metaborg.devenv:org.spoofax.terms")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.derive4j:derive4j-annotation")

    annotationProcessor("org.derive4j:derive4j")
}
