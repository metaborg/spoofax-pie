plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.spoofax3.core)
    api(libs.spoofax3.compiler)
    api(libs.metaborg.common)
    api(libs.metaborg.resource.api)
    api(libs.metaborg.pie.api)
    api(libs.metaborg.pie.task.archive)
    api(libs.metaborg.pie.task.java)

    // TODO: should the meta-languages use implementation configuration? We don't expose their API AFAICS.
    api(project(":cfg"))
    api(project(":sdf3"))
    api(project(":stratego"))
    api(project(":esv"))
    api(project(":statix"))
    api(project(":dynamix"))
    api(project(":sdf3_ext_statix"))
    api(project(":sdf3_ext_dynamix"))

    api(project(":strategolib"))
    api(project(":gpp"))
    api(project(":libspoofax2"))
    api(project(":libstatix"))

    // Using api configuration to make these annotations and processors available to javac that we call during
    // compilation, and to users of this library as well.
    api(libs.checkerframework.android)
    api(libs.dagger.compiler)


    compileOnly(libs.immutables.value.annotations)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
    annotationProcessor(libs.dagger.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.junit.params)
    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.metaborg.pie.serde.fst)
    testCompileOnly(libs.checkerframework.android)
}

tasks.test {
    enableAssertions = false // HACK: disable assertions until we support JSGLR2 parsing for Stratego
    // Show standard err in tests.
    testLogging {
        events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
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
