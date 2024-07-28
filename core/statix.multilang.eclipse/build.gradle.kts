plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.coronium.bundle")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    bundleTargetPlatformApi(eclipse("javax.inject"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.runtime"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.expressions"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.resources"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.filesystem"))

    bundleApi(project(":spoofax.eclipse"))

    bundleEmbedApi(project(":statix.multilang"))

    annotationProcessor("com.google.dagger:dagger-compiler")
    compileOnly("org.checkerframework:checker-qual-android")
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
