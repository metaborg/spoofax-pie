plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":jsglr.common"))
    api("org.metaborg.devenv:org.spoofax.jsglr")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.derive4j:derive4j-annotation")

    annotationProcessor("org.derive4j:derive4j")
}
