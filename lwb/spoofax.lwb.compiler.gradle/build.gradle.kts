plugins {
    id("org.metaborg.gradle.config.kotlin-gradle-plugin")
    kotlin("jvm")
    kotlin("kapt")
    id("org.gradle.kotlin.kotlin-dsl") // Same as `kotlin-dsl`, but without version, which is already set in root project.
}

metaborg {
    kotlinApiVersion = "1.3"
    kotlinLanguageVersion = "1.3"
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))
    compileOnly(platform(libs.metaborg.platform))

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
