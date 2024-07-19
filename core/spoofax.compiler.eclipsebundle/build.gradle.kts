plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.coronium.bundle")
}

mavenize {
    majorVersion.set("2022-06")
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

    bundleApi(project(":spoofax.eclipse"))
    bundleApi(project(":tooling.eclipsebundle"))

    bundleEmbedApi(project(":spoofax.compiler"))
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val exportPackage = listOf(
    // Already provided by `:tooling.eclipsebundle`
    "!mb.spoofax.compiler.interfaces.*",
    // Export `:spoofax.compiler` and `:spoofax.compiler.dagger`. Allow split package because `:spoofax.compiler.dagger`
    // generates dagger classes in the same package
    "mb.spoofax.compiler.*;-split-package:=merge-first",
    // Export `com.samskivert:jmustache` and `CachingMustacheCompiler` from `:spoofax.compiler`. Allow split package
    // because of the `CachingMustacheCompiler` class
    "com.samskivert.mustache.*;-split-package:=merge-first"
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
