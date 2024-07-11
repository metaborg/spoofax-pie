plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(project(":stratego.common"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.javax.inject)
    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}
