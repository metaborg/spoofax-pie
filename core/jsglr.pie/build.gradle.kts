plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    implementation(project(":aterm.common"))
    api(project(":jsglr.common"))
    api(project(":spoofax.core"))
    api("org.metaborg:pie.api")

    compileOnly("org.checkerframework:checker-qual-android")
    compileOnly("org.immutables:value-annotations")
    compileOnly("org.derive4j:derive4j-annotation")

    annotationProcessor("org.immutables:value")
    annotationProcessor("org.derive4j:derive4j")
}
