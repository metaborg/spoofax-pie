plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.jetbrains.intellij")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the IntelliJ plugin defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
