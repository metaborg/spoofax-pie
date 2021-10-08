plugins {
    java
    application
    kotlin("jvm") version "1.5.31"
    kotlin("kapt") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.21"
}

// Versions should match those in Coronium complex.platform
val logVersion = "0.3.0"
val slf4jVersion = "1.7.30"
val pieVersion = "0.9.0"
val daggerVersion = "2.36"
val commonsCsvVersion = "1.9.0"
val commonsIoVersion = "2.8.0"
val commonsMathVersion = "3.6.1"
val jacksonVersion = "2.13.0"
val cliktVersion = "3.2.0"
val logbackVersion = "1.2.6"
val microutilsLoggingVersion = "2.0.11"
val progressbarVersion = "0.9.2"

dependencies {
    implementation(project(":webdsl"))
    implementation("org.metaborg:pie.api:$pieVersion")
    implementation("org.metaborg:pie.runtime:$pieVersion")
    implementation("org.metaborg:pie.dagger:$pieVersion")
    implementation("org.metaborg:log.backend.slf4j:$logVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$microutilsLoggingVersion")
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.dagger:dagger-compiler:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("me.tongfei:progressbar:$progressbarVersion")

    implementation("org.apache.commons:commons-csv:$commonsCsvVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.apache.commons:commons-math3:$commonsMathVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

}

application {
    mainClass.set("mb.ccbench.webdsl.MainKt")
}

kapt {
    correctErrorTypes = true
}
