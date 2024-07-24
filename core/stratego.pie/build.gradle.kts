plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":stratego.common"))
    api("org.metaborg:pie.api")

    compileOnly("jakarta.inject:jakarta.inject-api")
    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.derive4j:derive4j-annotation")

    annotationProcessor("org.derive4j:derive4j")
}
