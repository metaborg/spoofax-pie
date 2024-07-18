import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
    maven("https://repo.gradle.org/gradle/libs-releases") // Required for Gradle tooling API.
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.common)
    api(project(":spoofax.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.resource.api)
    api(libs.metaborg.pie.api)
    api(libs.jmustache)
    api(libs.dagger)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.immutables.value)
    annotationProcessor(libs.derive4j)
    annotationProcessor(libs.dagger.compiler)

    testImplementation(libs.junit)
    testImplementation(project(":spoofax.compiler"))
    testImplementation(libs.dagger)
    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.junit.params)
    testImplementation(libs.jimfs)
//    testImplementation(libs.eclipse.jdt.core)
    testImplementation("org.eclipse.jdt:org.eclipse.jdt.core:3.33.0")
//    testImplementation("org.eclipse.jdt:org.eclipse.jdt.core") {
//        version {
//            strictly("3.25.0")
//        }
//    }
//    testImplementation("org.eclipse.platform:org.eclipse.core.runtime") {
//        version {
//            strictly("3.25.0")
//        }
//    }
    testImplementation(libs.gradle.tooling.api)
    testRuntimeOnly(libs.slf4j.simple) // SLF4J implementation required for Gradle tooling API.
    testCompileOnly(libs.checkerframework.android)
    testCompileOnly(libs.immutables.value.annotations) // Dagger accesses these annotations, which have class retention.
    testAnnotationProcessor(libs.dagger.compiler)
}

tasks.test {
    // Show standard out and err in tests, to ensure that failed Gradle builds are properly reported.
    testLogging {
        events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        showStandardStreams = true
    }
}

// Add generated resources directory as a resource source directory.
val generatedResourcesDir = project.buildDir.resolve("generated/resources")
sourceSets {
    main {
        resources {
            srcDir(generatedResourcesDir)
        }
    }
}

// Task that writes (dependency) versions to a versions.properties file, which is used in the Shared class.
val versionsPropertiesFile = generatedResourcesDir.resolve("version.properties")
val generateVersionPropertiesTask = tasks.register("generateVersionProperties") {
    inputs.property("version", project.version.toString())
    outputs.file(versionsPropertiesFile)
    doLast {
        val properties = NonShittyProperties()
        properties.setProperty("spoofax3", project.version.toString())
        versionsPropertiesFile.parentFile.run { if (!exists()) mkdirs() }
        versionsPropertiesFile.bufferedWriter().use {
            properties.storeWithoutDate(it)
        }
    }
}
tasks.compileJava.configure { dependsOn(generateVersionPropertiesTask) }
tasks.compileTestJava.configure { dependsOn(generateVersionPropertiesTask) }
tasks.processResources.configure { dependsOn(generateVersionPropertiesTask) }

// Custom properties class that does not write the current date, fixing incrementality.
class NonShittyProperties : Properties() {
    fun storeWithoutDate(writer: java.io.BufferedWriter) {
        val e: Enumeration<*> = keys()
        while (e.hasMoreElements()) {
            val key = e.nextElement()
            val value = get(key)
            writer.write("$key=$value")
            writer.newLine()
        }
        writer.flush()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
