// This is a separate project with Dagger components/modules, because the Dagger annotation processor cannot run on the
// main project, as there are staging conflicts with the other (org.immutables/derive4j) annotation processors.

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(project(":spoofax.compiler.spoofax2"))
    api(libs.dagger)

    compileOnly(libs.immutables.value.annotations) // Dagger accesses these annotations, which have class retention.

    annotationProcessor(libs.dagger.compiler)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
