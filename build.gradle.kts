plugins {
    id("org.metaborg.gradle.config.root-project") version "0.5.6"
    id("org.metaborg.gitonium") version "1.2.0"
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
    try {
        // New Develocity plugin
        setProperty("termsOfUseUrl", "https://gradle.com/help/legal-terms-of-use")
        setProperty("termsOfUseAgree", "yes")
    } catch (ex: groovy.lang.MissingPropertyException) {
        // Deprecated Gradle Enterprise plugin
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}

tasks.register("buildSpoofax3Lwb") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.root").task(":buildAll"))
}
gradle.includedBuild("spoofax3.lwb.distrib.root").let { lwbDistrib ->
    tasks.register("runSpoofax3LwbEclipse") {
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.feature:runEclipse"))
    }
    tasks.register("buildSpoofax3LwbEclipseInstallation") {
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.repository:createEclipseInstallation"))
    }
    tasks.register("buildSpoofax3LwbEclipseInstallationWithJvm") {
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.repository:createEclipseInstallationWithJvm"))
    }
    tasks.register("publishSpoofax3Lwb") {
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.repository:publish"))
    }
    tasks.register("archiveSpoofax3LwbEclipseInstallations") {
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.repository:archiveEclipseInstallations"))
        dependsOn(lwbDistrib.task(":spoofax.lwb.eclipse.repository:archiveEclipseInstallationsWithJvm"))
    }
}
