plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":spoofax.core"))
    api(project(":spoofax.compiler"))
    api("org.metaborg:resource")
    api("org.metaborg:pie.api")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.immutables:value-annotations")

    annotationProcessor("org.immutables:value")
}
