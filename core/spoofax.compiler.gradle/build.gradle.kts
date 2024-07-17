plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("kapt")
}

repositories {
    gradlePluginPortal() // Gradle plugin portal as repository for regular dependencies, as we depend on Gradle plugins.
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(project(":spoofax.compiler"))
    api(libs.dagger)

    implementation(libs.metaborg.pie.runtime)
    implementation(libs.metaborg.log.backend.slf4j)

    kapt(libs.dagger.compiler)
    compileOnly(libs.immutables.value.annotations) // Dagger accesses these annotations, which have class retention.

    // Dependencies to be able to configure the extensions provided by these Gradle plugins.
    compileOnly(libs.coronium)
    compileOnly(libs.bnd.gradle)
    compileOnly(libs.gradle.intellijPlugin)
}

gradlePlugin {
    plugins {
        create("spoofax-compiler-language") {
            id = "org.metaborg.spoofax.compiler.gradle.language"
            implementationClass = "mb.spoofax.compiler.gradle.plugin.LanguagePlugin"
        }
        create("spoofax-compiler-adapter") {
            id = "org.metaborg.spoofax.compiler.gradle.adapter"
            implementationClass = "mb.spoofax.compiler.gradle.plugin.AdapterPlugin"
        }
        create("spoofax-compiler-cli") {
            id = "org.metaborg.spoofax.compiler.gradle.cli"
            implementationClass = "mb.spoofax.compiler.gradle.plugin.CliPlugin"
        }
        create("spoofax-compiler-eclipse") {
            id = "org.metaborg.spoofax.compiler.gradle.eclipse"
            implementationClass = "mb.spoofax.compiler.gradle.plugin.EclipsePlugin"
        }
        create("spoofax-compiler-intellij") {
            id = "org.metaborg.spoofax.compiler.gradle.intellij"
            implementationClass = "mb.spoofax.compiler.gradle.plugin.IntellijPlugin"
        }
    }
}
