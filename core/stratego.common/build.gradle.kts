plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)
    api(libs.metaborg.resource.api)

    implementation(project(":aterm.common"))
    implementation(project(":jsglr.common"))

    api(libs.spoofax.terms)
    api(libs.interpreter.core)
    api(libs.strategoxt.strj)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}
