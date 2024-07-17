rootProject.name = "spoofax3.lwb.distrib.root"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
    }
}

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.8.1"
}


// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax.root) will include these composite builds.
if (gradle.parent == null) {
    includeBuildWithName("../core", "spoofax3.core.root")
    includeBuildWithName("../lwb", "spoofax3.lwb.root")
}

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

fun String.includeProject(id: String, path: String = "$this/$id") {
    include(id)
    project(":$id").projectDir = file(path)
}

"lang/rv32im".run {
    includeProject("rv32im")
    includeProject("rv32im.eclipse")
}

include("spoofax.lwb.eclipse")
include("spoofax.lwb.eclipse.feature")
include("spoofax.lwb.eclipse.repository")
