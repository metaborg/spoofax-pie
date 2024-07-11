plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)

    api(libs.statix.solver)

    implementation(project(":stratego.common"))
    implementation(project(":jsglr.common"))

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.immutables.value)
}
