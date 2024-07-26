plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.coronium.bundle")
}

mavenize {
    majorVersion.set("2022-06")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin (via Coronium) defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    bundleApi(libs.spoofax3.eclipse)

    bundleEmbedApi(project(":tiger"))
    bundleEmbedApi(project(":tiger.spoofax"))

    compileOnly(libs.checkerframework.android)
    annotationProcessor(libs.dagger.compiler)
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exports = listOf(
    // Provided by 'javax.inject' bundle.
    "!javax.inject.*",
    // Provided by 'spoofax.eclipse' bundle.
    "!mb.log.*",
    "!mb.resource.*",
    "!mb.pie.*",
    "!mb.common.*",
    "!mb.spoofax.core.*",
    "!dagger.*",
    // Do not export testing packages.
    "!junit.*",
    "!org.junit.*",
    // Do not export compile-time annotation packages.
    "!org.checkerframework.*",
    "!org.codehaus.mojo.animal_sniffer.*",
    // Allow split package for 'mb.nabl'.
    "mb.nabl2.*;-split-package:=merge-first",
    // Export packages from this project.
    "mb.tiger.eclipse.*",
    // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
    "*;provider=tiger;mandatory:=provider"
)
tasks {
    "jar"(Jar::class) {
        manifest {
            attributes(
                Pair("Export-Package", exports.joinToString(", "))
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
