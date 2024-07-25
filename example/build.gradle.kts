import org.metaborg.convention.Developer
import org.metaborg.convention.MavenPublishConventionExtension

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    id("org.metaborg.devenv.spoofax.gradle.langspec") version "0.1.37" apply false
    id("org.metaborg.coronium.bundle") version "0.4.0" apply false  // libs.plugins.coronium.bundle
    id("biz.aQute.bnd.builder") version "5.3.0" apply false         // libs.plugins.bnd.builder
    id("org.jetbrains.intellij") version "1.4.0" apply false        // libs.plugins.intellij
    id("com.palantir.graal") version "0.7.1" apply false

    id("org.metaborg.spoofax.compiler.gradle.language") apply false
    id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
    id("org.metaborg.spoofax.compiler.gradle.cli") apply false
    id("org.metaborg.spoofax.compiler.gradle.eclipse") apply false
    id("org.metaborg.spoofax.compiler.gradle.intellij") apply false
    id("org.metaborg.spoofax.compiler.gradle.spoofax2.language") apply false

    id("org.metaborg.spoofax.lwb.compiler.gradle.language") apply false
}

allprojects {
    apply(plugin = "org.metaborg.gitonium")

    // Configure Gitonium before setting the version
    gitonium {
        mainBranch.set("master")
    }

    version = gitonium.version
    group = "org.metaborg.devenv"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("spoofax-pie")

            metadata {
                inceptionYear.set("2017")
                developers.set(listOf(
                    Developer("Gohla", "Gabriel Konat", "gabrielkonat@gmail.com"),
                    Developer("AZWN", "Aron Zwaan", "aronzwaan@gmail.com"),
                    Developer("Virtlink", "Daniel A. A. Pelsmaeker", "developer@pelsmaeker.net"),
                    Developer("Apanatshka", "Jeff Smits", "mail@jeffsmits.net"),
                ))
            }
        }
    }
}
