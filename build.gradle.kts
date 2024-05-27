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
tasks.register("runSpoofax3LwbEclipse") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.feature:runEclipse"))
}
tasks.register("buildSpoofax3LwbEclipseInstallation") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.repository:createEclipseInstallation"))
}
tasks.register("buildSpoofax3LwbEclipseInstallationWithJvm") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.repository:createEclipseInstallationWithJvm"))
}
tasks.register("publishSpoofax3Lwb") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.repository:publish"))
}
tasks.register("archiveSpoofax3LwbEclipseInstallations") {
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.repository:archiveEclipseInstallations"))
    dependsOn(gradle.includedBuild("spoofax3.lwb.distrib.root").task(":spoofax.lwb.eclipse.repository:archiveEclipseInstallationsWithJvm"))
}
