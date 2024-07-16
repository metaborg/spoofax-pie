import org.metaborg.convention.MavenPublishConventionExtension

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    id("org.metaborg.coronium.bundle") version "0.4.0" apply false  // libs.plugins.coronium.bundle
    id("biz.aQute.bnd.builder") version "5.3.0" apply false         // libs.plugins.bnd.builder
    id("org.jetbrains.intellij") version "1.4.0" apply false        // libs.plugins.intellij
    `kotlin-dsl` apply false        // This puts the correct version of Kotlin on the classpath
}

val spoofax2Version: String = System.getProperty("spoofax2Version")
val spoofax2DevenvVersion: String = System.getProperty("spoofax2DevenvVersion")
allprojects {
    ext["spoofax2Version"] = spoofax2Version
    ext["spoofax2DevenvVersion"] = spoofax2DevenvVersion
}

allprojects {
    apply(plugin = "org.metaborg.gitonium")
    version = gitonium.version
    group = "org.metaborg"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("spoofax-pie")
        }
    }

    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}
