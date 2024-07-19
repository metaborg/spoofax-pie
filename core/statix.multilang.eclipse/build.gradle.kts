plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.coronium.bundle")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin defined its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    bundleTargetPlatformApi(eclipse("javax.inject"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.runtime"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.expressions"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.resources"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.filesystem"))

    bundleApi(project(":spoofax.eclipse"))

    bundleEmbedApi(project(":statix.multilang"))

    annotationProcessor(libs.dagger.compiler)
    compileOnly(libs.checkerframework.android)
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
    // Do not export compile-time annotation packages.
    "!org.checkerframework.*",
    "!org.codehaus.mojo.animal_sniffer.*",
    // Allow split package for 'mb.nabl2' and 'mb.statix'.
    "mb.nabl2.*;-split-package:=merge-first",
    "mb.statix.*;-split-package:=merge-first",
    // Export packages from this project.
    "mb.statix.multilang.eclipse.*",
    // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
    "*;provider=statix;mandatory:=provider"
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
