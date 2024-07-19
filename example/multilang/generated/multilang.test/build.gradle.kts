plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    testImplementation(libs.spoofax3.core)
    testImplementation(project(":minisdf"))
    testImplementation(project(":ministr"))

    testImplementation(libs.spoofax3.test)
    testCompileOnly(libs.checkerframework.android)
}

tasks.test {
    // Show standard out and err in tests.
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
        )
        showStandardStreams = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
