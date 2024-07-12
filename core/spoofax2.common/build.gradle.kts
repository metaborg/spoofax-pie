plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    implementation(libs.metaborg.common)
    implementation(project(":stratego.common"))
    implementation(libs.metaborg.util)

    api(libs.metaborg.resource.api)
    api(libs.metaborg.log.api)

    implementation(libs.interpreter.core)

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
