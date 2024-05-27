plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api("org.metaborg:common")
    api(project(":stratego.pie"))
    implementation(project(":aterm.common"))
    api(project(":spoofax.core"))
    api("org.metaborg:pie.api")

    compileOnly("org.checkerframework:checker-qual-android")
}
