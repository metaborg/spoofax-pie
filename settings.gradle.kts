// !! THIS FILE WAS GENERATED USING repoman !!
// Modify `repo.yaml` instead and use `repoman` to update this file
// See: https://github.com/metaborg/metaborg-gradle/

dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "latest.integration"
}

rootProject.name = "spoofax3.root"
includeBuild("core/") { name = "spoofax3.core.root" }
includeBuild("lwb/") { name = "spoofax3.lwb.root" }
includeBuild("lwb.distrib/") { name = "spoofax3.lwb.distrib.root" }
includeBuild("example/") { name = "spoofax3.lwb.eclipse.root" }
