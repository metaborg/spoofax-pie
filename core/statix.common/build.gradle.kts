plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)

    api(libs.statix.solver)

    implementation(project(":stratego.common"))
    implementation(project(":jsglr.common"))

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.immutables.value)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
