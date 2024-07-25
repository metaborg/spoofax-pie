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
    bundleTargetPlatformApi(eclipse("jakarta.inject-api"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.runtime"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.expressions"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.resources"))
    bundleTargetPlatformApi(eclipse("org.eclipse.core.filesystem"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.views"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.editors"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.console"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.workbench"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.workbench.texteditor"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.ide"))
    bundleTargetPlatformApi(eclipse("org.eclipse.ui.forms"))
    bundleTargetPlatformApi(eclipse("org.eclipse.jface.text"))
    bundleTargetPlatformApi(eclipse("org.eclipse.swt"))
    bundleTargetPlatformApi(eclipse("com.ibm.icu"))
    bundleTargetPlatformImplementation(eclipse("org.eclipse.ui.views.log"))

    bundleEmbedApi(libs.metaborg.common)
    bundleEmbedApi(project(":spoofax.core"))
    bundleEmbedApi(project(":spoofax.resource"))
    bundleEmbedApi(libs.metaborg.log.api)
    bundleEmbedApi(libs.metaborg.resource.api)
    bundleEmbedApi(libs.metaborg.pie.api)
    bundleEmbedApi(libs.metaborg.pie.runtime)
    bundleEmbedApi(libs.dagger)

    compileOnly(libs.checkerframework.android)
    annotationProcessor(libs.dagger.compiler)
}

// Use bnd to create a single OSGi bundle JAR that includes all dependencies.
val provider = "spoofax.eclipse"
val exportPackage = listOf(
    "mb.spoofax.eclipse.*",
    "mb.*;provider=$provider;mandatory:=provider",
    "dagger;provider=$provider;mandatory:=provider",
    "dagger.*;provider=$provider;mandatory:=provider"
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
