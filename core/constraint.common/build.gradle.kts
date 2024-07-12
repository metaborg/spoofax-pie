plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(project(":aterm.common"))
    api(project(":jsglr.common"))
    api(project(":stratego.common"))

    api(libs.metaborg.log.api)

    implementation(libs.nabl2.terms)
    implementation(libs.metaborg.util)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}
