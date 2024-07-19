plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("kapt")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":spoofax.compiler"))
    api(project(":spoofax.compiler.spoofax2"))
    api(project(":spoofax.compiler.spoofax2.dagger"))
    api(libs.dagger)
    implementation(libs.metaborg.pie.runtime)

    kapt(libs.dagger.compiler)
    compileOnly(libs.immutables.value.annotations) // Dagger accesses these annotations, which have class retention.

    // Dependencies to be able to use/configure the extensions provided by these Gradle plugins.
    compileOnly(project(":spoofax.compiler.gradle"))
    compileOnly(libs.spoofax3.gradle)
}

gradlePlugin {
    plugins {
        create("spoofax-compiler-spoofax2-language") {
            id = "org.metaborg.spoofax.compiler.gradle.spoofax2.language"
            implementationClass = "mb.spoofax.compiler.gradle.spoofax2.plugin.Spoofax2LanguagePlugin"
        }
    }
}
