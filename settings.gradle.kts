rootProject.name = "spoofax3.root"

// This allows us to use plugins from Metaborg Artifacts
pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

// This allows us to use the catalog in dependencies
dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    versionCatalogs {
        create("libs") {
            from("org.metaborg.spoofax3:catalog:0.3.3")
        }
    }
}

// This downloads an appropriate JVM if not already available
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
