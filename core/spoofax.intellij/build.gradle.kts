plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.jetbrains.intellij")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    api(project(":spoofax.core"))

    api(libs.metaborg.log.api)
    implementation(libs.metaborg.pie.runtime)
    implementation(libs.dagger)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
}

intellij {
    version.set("2019.3.2")
    instrumentCode.set(false)
}

// Skip non-incremental, slow, and unnecessary buildSearchableOptions task from IntelliJ.
tasks.getByName("buildSearchableOptions").enabled = false
