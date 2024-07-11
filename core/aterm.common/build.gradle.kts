plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)

    api(libs.spoofax.terms)

    // Depend on Stratego projects for access to standard library strategies which do aterm pretty printing.
    implementation(libs.interpreter.core)
    implementation(libs.strategoxt.strj)

    compileOnly(libs.checkerframework.android)
}
