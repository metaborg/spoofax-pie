import org.metaborg.convention.Person
import org.metaborg.convention.MavenPublishConventionExtension

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)
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

allprojects {
    apply(plugin = "org.metaborg.gitonium")

    // Configure Gitonium before setting the version
    gitonium {
        mainBranch.set("master")
    }

    version = gitonium.version
    group = "org.metaborg"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("spoofax-pie")

            metadata {
                inceptionYear.set("2017")
                developers.set(listOf(
                    Person("Gohla", "Gabriel Konat", "gabrielkonat@gmail.com"),
                    Person("AZWN", "Aron Zwaan", "aronzwaan@gmail.com"),
                    Person("Virtlink", "Daniel A. A. Pelsmaeker", "developer@pelsmaeker.net"),
                    Person("Apanatshka", "Jeff Smits", "mail@jeffsmits.net"),
                ))
            }
        }
    }
}
