plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    implementation(project(":aterm.common"))
    api(project(":jsglr.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
}
