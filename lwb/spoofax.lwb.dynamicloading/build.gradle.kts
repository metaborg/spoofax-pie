plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.spoofax3.core)
    api(libs.dagger)
    implementation(libs.metaborg.pie.runtime)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.derive4j)

    testImplementation(libs.slf4j.nop)
    testImplementation(project(":spoofax.lwb.compiler"))
    testImplementation(project(":spt"))
    testImplementation(project(":spt.dynamicloading"))
    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.metaborg.pie.serde.fst)
}

tasks.test {
    enableAssertions = false // HACK: disable assertions due to assertion in the Stratego compiler.
    testLogging {
        events(
            //org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
        )
    }
    jvmArgs(listOf(
        "-Xss16M",      // Set required stack size, mainly for serialization.
        // Needed for Java 17
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens", "java.base/java.math=ALL-UNNAMED",
        "--add-opens", "java.base/java.net=ALL-UNNAMED",
        "--add-opens", "java.base/java.text=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED",
    ))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
