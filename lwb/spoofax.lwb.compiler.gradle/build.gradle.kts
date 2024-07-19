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

    compileOnly(libs.spoofax3.compiler.gradle)
    api(project(":spoofax.lwb.compiler"))
    api(libs.dagger)
    implementation(libs.metaborg.pie.runtime)
    implementation(libs.metaborg.log.backend.slf4j)

    kapt(libs.dagger.compiler)
    compileOnly(libs.immutables.value.annotations) // Dagger accesses these annotations, which have class retention.
}

gradlePlugin {
    plugins {
        create("spoofax-lwb-compiler-language") {
            id = "org.metaborg.spoofax.lwb.compiler.gradle.language"
            implementationClass = "mb.spoofax.lwb.compiler.gradle.LanguagePlugin"
        }
    }
}
