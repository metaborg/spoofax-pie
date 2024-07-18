rootProject.name = "spoofax3.root"

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


// We split the build up into one main composite build in the 'core' directory, because it builds Gradle plugins,
// which we want to test. Gradle plugins are not directly available in a multi-project build, therefore a separate
// composite build is required. Included builds listed below can use the Gradle plugins built in 'core'.
includeBuildWithName("core", "spoofax3.core.root")
// The 'lwb' (language workbench) composite build includes the meta-languages which depend on Gradle plugins from the
// 'core' composite build. Additionally, 'lwb' also contains Gradle plugins for building languages.
includeBuildWithName("lwb", "spoofax3.lwb.root")
// The 'lwb.distrib' (language workbench distribution) composite build distributes the language workbench, which
// depends on Gradle plugins from the 'core' and 'lwb' composite builds.
includeBuildWithName("lwb.distrib", "spoofax3.lwb.distrib.root")
// The 'example' composite build has example languages, some based on Gradle plugins from 'core', and some based on
// Gradle plugins from 'lwb'. It also uses 'metalib'.
includeBuildWithName("example", "spoofax3.example.root")

fun includeBuildWithName(dir: String, name: String) {
    includeBuild(dir) {
        try {
            ConfigurableIncludedBuild::class.java
                .getDeclaredMethod("setName", String::class.java)
                .invoke(this, name)
        } catch (e: NoSuchMethodException) {
            // Running Gradle < 6, no need to set the name, ignore.
        }
    }
}
