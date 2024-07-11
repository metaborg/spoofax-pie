plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    implementation(project(":aterm.common"))
    api(project(":constraint.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}
