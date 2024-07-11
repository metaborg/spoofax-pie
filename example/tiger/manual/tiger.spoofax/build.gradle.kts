plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    // Platforms
    api(platform(compositeBuild("spoofax.depconstraints")))
    annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))
    testAnnotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

    // Main
    api(project(":tiger"))
    api(libs.spoofax3.core)
    api(libs.spoofax3.aterm.common)
    api(libs.spoofax3.jsglr.pie)
    api(libs.spoofax3.constraint.pie)
    api(libs.spoofax3.transform.pie)
    api(libs.spoofax3.spt.api)
    api(libs.metaborg.pie.api)
    api(libs.metaborg.pie.dagger)
    api(libs.dagger)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)

    // Test
    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}
