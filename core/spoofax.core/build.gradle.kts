plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(project(":spoofax.common"))
    api(libs.metaborg.common)
    api(libs.metaborg.log.api)
    api(libs.metaborg.log.dagger)
    api(libs.metaborg.resource.api)
    api(libs.metaborg.resource.dagger)
    api(libs.spoofax3.resource)
    api(libs.metaborg.pie.api)
    api(libs.metaborg.pie.graph)
    api(libs.metaborg.pie.dagger)
    api(libs.dagger)

    compileOnly(libs.immutables.value.annotations)
    compileOnly(libs.derive4j.annotation)
    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
}
