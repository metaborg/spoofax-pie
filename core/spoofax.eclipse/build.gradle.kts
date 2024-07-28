plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.coronium.bundle")
}

mavenize {
    majorVersion.set("2022-06")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    bundleTargetPlatformApi(eclipse("javax.inject"))
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

    bundleEmbedApi("org.metaborg:common")
    bundleEmbedApi(project(":spoofax.core"))
    bundleEmbedApi(project(":spoofax.resource"))
    bundleEmbedApi("org.metaborg:log.api")
    bundleEmbedApi("org.metaborg:resource")
    bundleEmbedApi("org.metaborg:pie.api")
    bundleEmbedApi("org.metaborg:pie.runtime")
    bundleEmbedApi("com.google.dagger:dagger")

    compileOnly("org.checkerframework:checker-qual-android")
    annotationProcessor("com.google.dagger:dagger-compiler")
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
