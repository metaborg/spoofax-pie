plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":jsglr.common"))
    api(project(":stratego.common"))
    api(project(":spoofax.core"))

    api(libs.statix.solver)
    api(libs.metaborg.pie.api)
    api(libs.snakeyaml)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.immutables.value)
}
