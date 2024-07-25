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
    bundleEmbedApi(platform(libs.metaborg.platform))

    bundleTargetPlatformApi(eclipse("jakarta.inject-api"))

    bundleApi(project(":spoofax.eclipse"))

    bundleEmbedApi(project(":aterm.common"))
    bundleEmbedApi(project(":constraint.common"))
    bundleEmbedApi(project(":constraint.pie"))
    bundleEmbedApi(project(":esv.common"))
    bundleEmbedApi(project(":jsglr.common"))
    bundleEmbedApi(project(":jsglr.pie"))
    bundleEmbedApi(project(":jsglr1.common"))
    bundleEmbedApi(project(":jsglr2.common"))
    bundleEmbedApi(project(":nabl2.common"))
    bundleEmbedApi(project(":spoofax2.common"))
    bundleEmbedApi(project(":statix.common"))
    bundleEmbedApi(project(":statix.pie"))
    bundleEmbedApi(project(":statix.codecompletion"))
    bundleEmbedApi(project(":statix.codecompletion.pie"))
    bundleEmbedApi(project(":stratego.common"))
    bundleEmbedApi(project(":stratego.pie"))
    bundleEmbedApi(project(":spt.api"))
    bundleEmbedApi(project(":tego.runtime"))
    bundleEmbedApi(libs.metaborg.pie.task.archive)

    bundleEmbedApi(project(":spoofax.compiler.interfaces"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exportPackage = listOf(
    // Provided by `jakarta.inject`
    "!jakarta.inject.*",
    // Provided by `:spoofax.eclipse`
    "!mb.log.*",
    "!mb.resource.*",
    "!mb.pie.api.*",
    "!mb.pie.runtime.*",
    "!mb.common.*",
    "!mb.spoofax.core.*",
    "!dagger.*",
    // Do not export testing packages.
    "!junit.*",
    "!org.junit.*",
    // Do not export compile-time annotation packages.
    "!org.checkerframework.*",
    "!org.codehaus.mojo.animal_sniffer.*",
    // Allow split package for 'mb.nabl2'.
    "mb.nabl2.*;-split-package:=merge-first",
    // Export our own package
    "mb.tooling.eclipsebundle",
    // Export what is left, using a mandatory provider to prevent accidental imports via 'Import-Package'.
    "*;provider=tooling.eclipsebundle;mandatory:=provider"
)
tasks {
    "jar"(Jar::class) {
        manifest {
            attributes(
                Pair("Export-Package", exportPackage.joinToString(", "))
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
