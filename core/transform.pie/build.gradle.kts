plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(project(":stratego.pie"))
    implementation(project(":aterm.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
}
