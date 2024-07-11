plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
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
