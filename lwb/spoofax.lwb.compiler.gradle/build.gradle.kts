plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("kapt")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(libs.metaborg.platform))

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
