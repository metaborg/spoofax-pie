plugins {
    id("org.metaborg.gradle.config.root-project") version "0.5.6"
    id("org.metaborg.gitonium") version "1.2.0"

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    id("org.metaborg.gradle.config.kotlin-gradle-plugin") version "0.5.6" apply false
    id("org.metaborg.coronium.bundle") version "0.3.17" apply false
    id("biz.aQute.bnd.builder") version "5.3.0" apply false
    id("org.jetbrains.intellij") version "1.4.0" apply false
    kotlin("jvm") version "1.4.20" apply false // Use 1.4.20 to keep in sync with embedded Kotlin version of Gradle 6.8.
    `kotlin-dsl` apply false
}

subprojects {
    metaborg {
        configureSubProject()
    }
}

val spoofax2Version: String = System.getProperty("spoofax2Version")
val spoofax2DevenvVersion: String = System.getProperty("spoofax2DevenvVersion")
allprojects {
    ext["spoofax2Version"] = spoofax2Version
    ext["spoofax2DevenvVersion"] = spoofax2DevenvVersion
}
